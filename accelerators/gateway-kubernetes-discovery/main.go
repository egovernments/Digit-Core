/*
Copyright © 2019 NAME HERE <EMAIL ADDRESS>

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

	http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package main

import (
	"context"
	"fmt"
	"log"
	"os"
	"sort"
	"strings"
	"text/template"

	v1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/client-go/kubernetes"

	"k8s.io/client-go/rest"
)

// Route comprising of attributes to build a zuul route object
type Route struct {
	Host          string
	Path          string // Annotation value of zuul/route-path pointing to the context to be filtered
	Namespace     string // Kubernetes namespace the service belongs to
	ServiceURL    string // Service URL to be redirected to
	RateLimiter   bool
	KeyResolver   string
	ReplenishRate string
	BurstCapacity string
}

const gatewayKeyResolver = "gateway-keyResolver"
const gatewayReplenishRate = "gateway-replenishRate"
const gatewayBurstCapacity = "gateway-burstCapacity"
const sAnnotationPath string = "zuul/route-path"
const sAnnotationHost string = "zuul/route-host"
const sAnnotationInternalGatewayEnabled string = "internal-gateway-enabled"
const sAnnotationInternalGatewayService string = "internal-gateway-service"
const routesTemplate string = `{{- range $index, $route := . }}
spring.cloud.gateway.routes[{{ $index }}].id={{ $route.Path }}-{{ $route.Namespace }}
spring.cloud.gateway.routes[{{ $index }}].uri={{ $route.ServiceURL }}
spring.cloud.gateway.routes[{{ $index }}].predicates[0]=Path=/{{ $route.Path }}/**
{{ if ne $route.Host "" }}spring.cloud.gateway.routes[{{ $index }}].predicates[1]=Host={{ $route.Host }}{{ end }}
{{ if $route.RateLimiter }}spring.cloud.gateway.routes[{{ $index }}].filters[0].name=RequestRateLimiter
{{ if ne $route.KeyResolver "" }}spring.cloud.gateway.routes[{{ $index }}].filters[0].args.redis-rate-limiter.keyResolver="#{{ "{" }}{{ $route.KeyResolver }}{{ "}" }}"{{ end }}
{{ if ne $route.ReplenishRate "" }}spring.cloud.gateway.routes[{{ $index }}].filters[0].args.redis-rate-limiter.replenishRate={{ $route.ReplenishRate }}{{ end }}
{{ if ne $route.BurstCapacity "" }}spring.cloud.gateway.routes[{{ $index }}].filters[0].args.redis-rate-limiter.burstCapacity={{ $route.BurstCapacity }}{{ end }}
{{ end }}{{ end }}`

func getKubeConnection() (clientset *kubernetes.Clientset) {

	config, err := rest.InClusterConfig()
	if err != nil {
		panic(err)
	}
	clientset, err = kubernetes.NewForConfig(config)
	if err != nil {
		panic(err)
	}

	return clientset
}

func listAllServices(clientset *kubernetes.Clientset, namespace string) (s *v1.ServiceList) {
	sc := clientset.CoreV1().Services(namespace)

	s, err := sc.List(context.TODO(), metav1.ListOptions{})
	if err != nil {
		panic(err)
	}

	return s
}

func getRoutes(s *v1.ServiceList, defaultInternalGatewayService string) (r *[]Route) {
	routes := []Route{}
	for _, s := range s.Items {

		if s.Annotations != nil {
			if val, ok := s.Annotations[sAnnotationPath]; ok {
				path := fmt.Sprintf("%s", val)
				port := s.Spec.Ports[0].Port
				url := fmt.Sprintf("http://%s.%s:%d/", s.Name, s.Namespace, port)

				if s.Annotations[sAnnotationInternalGatewayEnabled] == "true" {
					gatewayService := defaultInternalGatewayService
					if svc, ok := s.Annotations[sAnnotationInternalGatewayService]; ok && svc != "" {
						gatewayService = svc
					}
					if gatewayService != "" {
						url = fmt.Sprintf("http://%s:%d/", gatewayService, port)
					}
				}

				// Initialize variables for rate limiter annotations
				host := ""
				rateLimiter := false
				keyResolver := ""
				replenishRate := ""
				burstCapacity := ""
				if val, ok := s.Annotations[sAnnotationHost]; ok {
					host = val
				}
				if val, ok := s.Annotations[gatewayKeyResolver]; ok {
					rateLimiter = true
					keyResolver = val
				}
				if val, ok := s.Annotations[gatewayReplenishRate]; ok {
					rateLimiter = true
					replenishRate = val
				}
				if val, ok := s.Annotations[gatewayBurstCapacity]; ok {
					rateLimiter = true
					burstCapacity = val
				}

				namespace := s.Namespace
				routes = append(routes, Route{host, path, namespace, url, rateLimiter, keyResolver, replenishRate, burstCapacity})
			}
		}
	}

	return &routes

}

func writeTemplate(r *[]Route) {
	path, _ := os.LookupEnv("OUTPUT_FILE_PATH")
	f, err := os.Create(path)
	if err != nil {
		panic(err)
	}

	tmpl, err := template.New("test").Parse(routesTemplate)
	if err != nil {
		panic(err)
	}

	err = tmpl.Execute(f, *r)
	if err != nil {
		panic(err)
	}

	f.Close()
}

func logRoutes(routes []Route) {
	log.Printf("Total routes configured: %d", len(routes))
	for i, r := range routes {
		host := "-"
		if r.Host != "" {
			host = r.Host
		}
		rateLimit := "-"
		if r.RateLimiter {
			rateLimit = fmt.Sprintf("replenishRate=%s burstCapacity=%s", r.ReplenishRate, r.BurstCapacity)
		}
		log.Printf("[%d] id=%-s | uri=%s | host=%s | rateLimit=%s",
			i, r.Path+"-"+r.Namespace, r.ServiceURL, host, rateLimit)
	}
}

func routeID(r Route) string {
	return r.Path + "-" + r.Namespace
}

type shadowFinding struct {
	index      int
	shadowedBy int
}

func pathCovers(a, b string) bool {
	return a == b || strings.HasPrefix(b, a+"/")
}

func hostMatches(pattern, host string) bool {
	if pattern == host {
		return true
	}
	if strings.HasPrefix(pattern, "*.") {
		return strings.HasSuffix(host, pattern[1:])
	}
	return false
}

func hostCovers(a, b string) bool {
	if a == "" {
		return true
	}
	if b == "" {
		return false
	}
	patterns := strings.Split(a, ",")
	for _, h := range strings.Split(b, ",") {
		h = strings.TrimSpace(h)
		covered := false
		for _, p := range patterns {
			if hostMatches(strings.TrimSpace(p), h) {
				covered = true
				break
			}
		}
		if !covered {
			return false
		}
	}
	return true
}

func routeCovers(a, b Route) bool {
	return pathCovers(a.Path, b.Path) && hostCovers(a.Host, b.Host)
}

func findShadowedRoutes(routes []Route) []shadowFinding {
	findings := []shadowFinding{}
	for j := 1; j < len(routes); j++ {
		for i := 0; i < j; i++ {
			if routeCovers(routes[i], routes[j]) {
				findings = append(findings, shadowFinding{j, i})
				break
			}
		}
	}
	return findings
}

func runRouteValidation(routes []Route) {
	mode := strings.ToLower(os.Getenv("ROUTE_VALIDATION_MODE"))
	if mode == "" {
		mode = "warn"
	}
	if mode != "warn" && mode != "strict" {
		log.Printf("Unknown ROUTE_VALIDATION_MODE %q, defaulting to warn (expected warn|strict)", mode)
		mode = "warn"
	}

	log.Printf("Validating %d routes for unreachable (shadowed) routes (mode=%s)", len(routes), mode)
	findings := findShadowedRoutes(routes)
	shadowedIDs := []string{}
	for _, f := range findings {
		shadowed := routes[f.index]
		by := routes[f.shadowedBy]
		log.Printf("UNREACHABLE ROUTE id=%s (index %d) shadowed by id=%s (index %d): every request matching Path=/%s/** Host=%q is consumed first by Path=/%s/** Host=%q",
			routeID(shadowed), f.index, routeID(by), f.shadowedBy,
			shadowed.Path, shadowed.Host, by.Path, by.Host)
		shadowedIDs = append(shadowedIDs, routeID(shadowed))
	}
	log.Printf("Route validation result: %d/%d routes unreachable", len(findings), len(routes))

	if len(findings) > 0 && mode == "strict" {
		log.Panicf("Route validation failed: %d unreachable route(s): %s", len(findings), strings.Join(shadowedIDs, ", "))
	}
}

// Get all kubernetes services in the cluster using config serviceaccount
// Filter services with annotation "zuul/route-path"
// Build zuul routing configuration using configured template file
// Write file to "OUTPUT_FILE_PATH" path
func main() {
	if _, ok := os.LookupEnv("OUTPUT_FILE_PATH"); !ok {
		log.Panicln("OUTPUT_FILE_PATH environment variable not set! Exiting!")
	}
	n, ok := os.LookupEnv("NAMESPACE")
	if !ok {
		log.Println("NAMESPACE environment vairable not set, defaulting to cluster wide")
	}

	defaultInternalGatewayService, ok := os.LookupEnv("DEFAULT_INTERNAL_GATEWAY_SERVICE")
	if !ok {
		log.Println("DEFAULT_INTERNAL_GATEWAY_SERVICE not set, internal-gateway-enabled services without annotation will use their direct service URL")
	}

	clientset := getKubeConnection()

	namespaces := strings.Split(n, ",")
	routes := []Route{}
	for _, namespace := range namespaces {
		s := listAllServices(clientset, namespace)
		r := getRoutes(s, defaultInternalGatewayService)
		routes = append(routes, *r...)
	}

	// Sort: within the same path, routes with a Host predicate come before path-only routes
	sort.SliceStable(routes, func(i, j int) bool {
		if routes[i].Path != routes[j].Path {
			return routes[i].Path < routes[j].Path
		}
		iHasHost := routes[i].Host != ""
		jHasHost := routes[j].Host != ""
		if iHasHost != jHasHost {
			return iHasHost // host routes sort before path-only routes
		}
		return false
	})

	logRoutes(routes)
	writeTemplate(&routes)
	runRouteValidation(routes)
}
