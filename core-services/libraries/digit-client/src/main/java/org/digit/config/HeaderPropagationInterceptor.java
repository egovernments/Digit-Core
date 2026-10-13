package org.digit.config;

import org.digit.util.HeaderStore;
import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class HeaderPropagationInterceptor
implements ClientHttpRequestInterceptor {
    private static final Logger log = LoggerFactory.getLogger(HeaderPropagationInterceptor.class);
    private final PropagationProperties propagationProperties;

    public HeaderPropagationInterceptor(PropagationProperties propagationProperties) {
        this.propagationProperties = propagationProperties;
    }

    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        System.out.println("\ud83d\udd0d HeaderPropagationInterceptor called for request: " + String.valueOf(request.getMethod()) + " " + String.valueOf(request.getURI()));
        try {
            Map<String, String> headersToPropagate = HeaderStore.getHeadersToPropagate(this.propagationProperties);
            System.out.println("\ud83d\udd0d Headers to propagate: " + String.valueOf(headersToPropagate));
            System.out.println("\ud83d\udd0d PropagationProperties allow list: " + String.valueOf(this.propagationProperties.getAllow()));
            System.out.println("\ud83d\udd0d PropagationProperties prefixes: " + String.valueOf(this.propagationProperties.getPrefixes()));
            System.out.println("\ud83d\udd0d PropagationProperties object: " + String.valueOf(this.propagationProperties));
            System.out.println("\ud83d\udd0d PropagationProperties class: " + String.valueOf(this.propagationProperties.getClass()));
            if (headersToPropagate != null && !headersToPropagate.isEmpty()) {
                HttpHeaders headers = request.getHeaders();
                int propagatedCount = 0;
                for (Map.Entry<String, String> entry : headersToPropagate.entrySet()) {
                    String headerName = entry.getKey();
                    String headerValue = entry.getValue();
                    System.out.println("\ud83d\udd0d Checking header: " + headerName + " = " + headerValue);
                    System.out.println("\ud83d\udd0d Should propagate? " + this.propagationProperties.shouldPropagate(headerName));
                    if (this.propagationProperties.shouldPropagate(headerName)) {
                        headers.add(headerName, headerValue);
                        ++propagatedCount;
                        System.out.println("\u2705 Added header: " + headerName + " = " + headerValue);
                        continue;
                    }
                    System.out.println("\u274c Skipped header: " + headerName + " (not in allow list)");
                }
                System.out.println("\ud83d\udd0d Propagated " + propagatedCount + " headers to outbound request to " + String.valueOf(request.getURI()));
            } else {
                System.out.println("\u26a0\ufe0f No headers to propagate found in request context");
            }
        }
        catch (Exception e) {
            log.error("\ud83d\udd0d Failed to propagate headers: {}", (Object)e.getMessage(), (Object)e);
        }
        return execution.execute(request, body);
    }
}

