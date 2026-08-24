package org.egov.bundler;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Reads package.yaml at the repo root, resolves each declared bundle against
 * per-service service.yaml files, and emits bundles/&lt;name&gt;/ containing a
 * Spring Boot module (pom.xml, main class, application.properties) that
 * composes the included services in-process.
 *
 * Invocation:
 *   mvn digit-bundler:generate                   (all bundles)
 *   mvn digit-bundler:generate -Dbundle=NAME     (single bundle)
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.NONE, requiresProject = true, aggregator = true)
public class GenerateMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(property = "bundle")
    private String bundleName;

    @Parameter(property = "packageFile", defaultValue = "package.yaml")
    private String packageFile;

    @Parameter(property = "bundlesDir", defaultValue = "bundles")
    private String bundlesDir;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Path repoRoot = findRepoRoot();
        Path pkgFile = repoRoot.resolve(packageFile);
        if (!Files.exists(pkgFile)) {
            throw new MojoFailureException("package.yaml not found at " + pkgFile);
        }

        Map<String, Object> pkg = loadYaml(pkgFile);
        Map<String, Object> bundles = asMap(pkg.get("bundles"));
        if (bundles == null || bundles.isEmpty()) {
            getLog().warn("No bundles declared in " + pkgFile);
            return;
        }

        List<String> targets = (bundleName != null && !bundleName.isBlank())
                ? List.of(bundleName)
                : new ArrayList<>(bundles.keySet());

        for (String name : targets) {
            Object spec = bundles.get(name);
            if (spec == null) {
                throw new MojoFailureException("Bundle '" + name + "' not found in package.yaml");
            }
            generateBundle(repoRoot, name, asMap(spec));
        }
    }

    private void generateBundle(Path repoRoot, String name, Map<String, Object> spec) throws MojoExecutionException {
        getLog().info("---- Generating bundle: " + name + " ----");
        String type = str(spec.get("type"), "monolith");
        @SuppressWarnings("unchecked")
        List<String> include = (List<String>) spec.get("include");
        if (include == null || include.isEmpty()) {
            throw new MojoExecutionException("Bundle '" + name + "' has empty include");
        }

        List<Map<String, Object>> serviceSpecs = new ArrayList<>();
        Map<String, Map<String, Object>> serviceCoords = new LinkedHashMap<>();
        Set<String> scanPackages = new LinkedHashSet<>();

        for (String svc : include) {
            Path svcDir = repoRoot.resolve(svc);
            Path svcYaml = svcDir.resolve("service.yaml");
            if (!Files.exists(svcYaml)) {
                throw new MojoExecutionException("service.yaml missing for " + svc + " at " + svcYaml);
            }
            Map<String, Object> svcMeta = loadYaml(svcYaml);
            // Synthesize the "name" from the include: entry (which is the directory
            // name). service.yaml no longer carries `name` — the include: string is
            // authoritative and identical, so we inject it here for downstream
            // callers (property-conflict warning attribution).
            svcMeta.put("name", svc);
            svcMeta.put("__dir", svcDir.toString());
            serviceSpecs.add(svcMeta);

            @SuppressWarnings("unchecked")
            List<String> pkgs = (List<String>) svcMeta.getOrDefault("scan-base-packages", List.of());
            scanPackages.addAll(pkgs);

            serviceCoords.put(svc, readMavenCoords(svcDir.resolve("pom.xml")));
        }

        warnOnPropertyConflicts(serviceSpecs, spec);

        Path bundleDir = repoRoot.resolve(bundlesDir).resolve(name);
        try {
            Files.createDirectories(bundleDir.resolve("src/main/java/org/egov/bundle"));
            Files.createDirectories(bundleDir.resolve("src/main/resources"));

            String appClassName = toClassName(name) + "Application";
            writePom(bundleDir.resolve("pom.xml"), name, serviceCoords);
            writeMainClass(bundleDir.resolve("src/main/java/org/egov/bundle/" + appClassName + ".java"),
                    appClassName, scanPackages);
            writeAppProps(bundleDir.resolve("src/main/resources/application.properties"),
                    spec, serviceSpecs);
            writeProfileOverrides(bundleDir.resolve("src/main/resources"), spec);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to write bundle files for " + name, e);
        }

        registerModuleInRootPom(repoRoot.resolve("pom.xml"), bundlesDir + "/" + name);

        getLog().info("Bundle '" + name + "' generated:");
        getLog().info("  type          = " + type);
        getLog().info("  services      = " + include);
        getLog().info("  scan-packages = " + scanPackages);
        getLog().info("  output        = " + bundleDir);
    }

    /**
     * Warn on any property key set with different values across two or more services'
     * defaults files. In monolith mode Spring Boot loads imports in declared order, so
     * the LAST-listed service wins silently — this makes those clashes visible at
     * generate-time so the user can decide.
     *
     * If the bundle's package.yaml `properties:` block already sets the key, it's
     * considered explicitly resolved and no warning is emitted (application-&lt;profile&gt;
     * .properties overrides all imports anyway).
     */
    private void warnOnPropertyConflicts(List<Map<String, Object>> serviceSpecs, Map<String, Object> bundleSpec) {
        Map<String, Object> bundleOverrides = asMap(bundleSpec.get("properties"));
        Set<String> resolved = bundleOverrides == null ? Set.of() : bundleOverrides.keySet();

        // Preserve declared order — index of each service in the include: list.
        List<String> orderedServices = new ArrayList<>();
        Map<String, Map<String, String>> keyOccurrences = new LinkedHashMap<>();

        for (Map<String, Object> svc : serviceSpecs) {
            String svcName = str(svc.get("name"), "<unknown>");
            orderedServices.add(svcName);
            String defaultsFile = str(svc.get("defaults-file"), null);
            String svcDir = str(svc.get("__dir"), null);
            if (defaultsFile == null || svcDir == null) continue;
            Path defaultsPath = Path.of(svcDir, "src", "main", "resources", defaultsFile);
            if (!Files.exists(defaultsPath)) continue;
            Properties p = new Properties();
            try (InputStream in = Files.newInputStream(defaultsPath)) {
                p.load(in);
            } catch (IOException e) {
                getLog().warn("Could not read " + defaultsPath + ": " + e.getMessage());
                continue;
            }
            for (String key : p.stringPropertyNames()) {
                keyOccurrences.computeIfAbsent(key, k -> new LinkedHashMap<>())
                        .put(svcName, p.getProperty(key));
            }
        }

        int conflicts = 0;
        for (Map.Entry<String, Map<String, String>> e : keyOccurrences.entrySet()) {
            Map<String, String> occurrences = e.getValue();
            if (occurrences.size() < 2) continue;
            if (new HashSet<>(occurrences.values()).size() < 2) continue; // same value everywhere
            if (resolved.contains(e.getKey())) continue; // bundle explicitly overrides

            String winner = null;
            for (String svcName : orderedServices) {
                if (occurrences.containsKey(svcName)) winner = svcName;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Property conflict: '").append(e.getKey()).append("'");
            for (Map.Entry<String, String> occ : occurrences.entrySet()) {
                sb.append("\n    ").append(occ.getKey()).append(" = ").append(occ.getValue());
            }
            sb.append("\n    -> effective value: from ").append(winner)
                    .append(" (last in include order). Override in package.yaml `properties:` to silence.");
            getLog().warn(sb.toString());
            conflicts++;
        }
        if (conflicts > 0) {
            getLog().warn(conflicts + " property conflict(s) detected across included services' defaults files.");
        }
    }

    private void writePom(Path out, String bundleName, Map<String, Map<String, Object>> serviceCoords) throws IOException {
        StringBuilder deps = new StringBuilder();
        for (Map<String, Object> c : serviceCoords.values()) {
            deps.append("        <dependency>\n")
                    .append("            <groupId>").append(c.get("groupId")).append("</groupId>\n")
                    .append("            <artifactId>").append(c.get("artifactId")).append("</artifactId>\n")
                    .append("            <version>").append(c.get("version")).append("</version>\n")
                    .append("        </dependency>\n");
        }
        String pom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>

                    <parent>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-parent</artifactId>
                        <version>3.4.5</version>
                        <relativePath/>
                    </parent>

                    <groupId>org.egov.bundle</groupId>
                    <artifactId>%s</artifactId>
                    <version>1.0.0-SNAPSHOT</version>
                    <name>%s</name>
                    <description>Generated by digit-bundler. Do not edit.</description>

                    <properties>
                        <java.version>17</java.version>
                    </properties>

                    <dependencies>
                %s    </dependencies>

                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.springframework.boot</groupId>
                                <artifactId>spring-boot-maven-plugin</artifactId>
                                <configuration>
                                    <mainClass>org.egov.bundle.%sApplication</mainClass>
                                </configuration>
                            </plugin>
                        </plugins>
                    </build>
                </project>
                """.formatted(bundleName, bundleName, deps.toString(), toClassName(bundleName));
        Files.writeString(out, pom);
    }

    private void writeMainClass(Path out, String className, Set<String> scanPackages) throws IOException {
        // Deduplicate the scan roots — remove any package that is a subpackage of another
        // already in the set. Overlapping roots cause Spring to visit the same class file
        // multiple times, which combined with @SpringBootConfiguration meta-scans can
        // register the same bean under different names.
        Set<String> minimalPackages = minimalCovering(scanPackages);
        String packagesLiteral = minimalPackages.stream()
                .map(p -> "\"" + p + "\"")
                .collect(Collectors.joining(", "));
        // Bean-collision safety: FullyQualifiedAnnotationBeanNameGenerator names each
        // component-scanned bean by its FQN, not its simple name — so two services
        // sharing a scan root can each ship a class called (e.g.) NotificationService
        // without startup collision. Type-based autowiring stays intact because the
        // Java types are still distinct across packages.
        //
        // Excluding @SpringBootConfiguration-annotated classes is essential: any such
        // class on the classpath (each included service's main + any transitive
        // library's @SpringBootApplication, e.g. mdms-client's MdmsClientApplication)
        // carries its own meta-@ComponentScan with default naming. If left in the scan,
        // Spring picks them up as @Configuration and fires those secondary scans,
        // registering every scanned class TWICE (once FQN, once simple-name). The
        // ANNOTATION filter catches all such classes generically.
        //
        // We use @SpringBootConfiguration (not @SpringBootApplication) as the exclude
        // marker because @SpringBootApplication is meta-annotated with @SBC — filtering
        // on @SBC catches both.
        String body = """
                package org.egov.bundle;

                import org.springframework.boot.SpringApplication;
                import org.springframework.boot.SpringBootConfiguration;
                import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
                import org.springframework.context.annotation.ComponentScan;
                import org.springframework.context.annotation.FilterType;
                import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;

                @SpringBootConfiguration
                @EnableAutoConfiguration
                @ComponentScan(
                    basePackages = { %s },
                    nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class,
                    excludeFilters = @ComponentScan.Filter(
                        type = FilterType.ANNOTATION,
                        classes = SpringBootConfiguration.class
                    )
                )
                public class %s {
                    public static void main(String[] args) {
                        SpringApplication.run(%s.class, args);
                    }
                }
                """.formatted(packagesLiteral, className, className);
        Files.writeString(out, body);
    }

    /** Keep only packages that are not a proper prefix of another package in the set. */
    private Set<String> minimalCovering(Set<String> pkgs) {
        Set<String> keep = new LinkedHashSet<>();
        for (String p : pkgs) {
            boolean isChildOfSomethingElse = pkgs.stream()
                    .anyMatch(q -> !q.equals(p) && p.startsWith(q + "."));
            if (!isChildOfSomethingElse) keep.add(p);
        }
        return keep;
    }

    private void writeAppProps(Path out, Map<String, Object> spec, List<Map<String, Object>> services)
            throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("# Generated by digit-bundler. Do not edit; regenerate via `mvn digit-bundler:generate`.\n\n");

        @SuppressWarnings("unchecked")
        List<String> profiles = (List<String>) spec.getOrDefault("spring-profiles", List.of());
        if (!profiles.isEmpty()) {
            sb.append("spring.profiles.active=").append(String.join(",", profiles)).append("\n");
        }

        Map<String, Object> server = asMap(spec.get("server"));
        if (server != null && server.get("port") != null) {
            sb.append("server.port=").append(server.get("port")).append("\n");
        }

        List<String> imports = services.stream()
                .map(s -> str(s.get("defaults-file"), null))
                .filter(f -> f != null && !f.isBlank())
                .map(f -> "classpath:" + f)
                .toList();
        if (!imports.isEmpty()) {
            sb.append("spring.config.import=").append(String.join(",", imports)).append("\n");
        }

        Files.writeString(out, sb.toString());
    }

    /**
     * Bundle-level `properties:` from package.yaml go into application-&lt;profile&gt;.properties,
     * which Spring Boot loads AFTER the base application.properties and its spring.config.import
     * chain — so bundle values always override defaults.
     */
    private void writeProfileOverrides(Path resourcesDir, Map<String, Object> spec) throws IOException {
        Map<String, Object> extraProps = asMap(spec.get("properties"));
        if (extraProps == null || extraProps.isEmpty()) return;

        @SuppressWarnings("unchecked")
        List<String> profiles = (List<String>) spec.getOrDefault("spring-profiles", List.of());
        String targetFile = profiles.isEmpty()
                ? "application.properties"           // no profile: append to base
                : "application-" + profiles.get(0) + ".properties";

        Path out = resourcesDir.resolve(targetFile);
        StringBuilder sb = new StringBuilder();
        sb.append("# Generated by digit-bundler. Do not edit; regenerate via `mvn digit-bundler:generate`.\n");
        sb.append("# Bundle-level overrides (from package.yaml) — loaded after all imports.\n\n");
        extraProps.forEach((k, v) -> sb.append(k).append("=").append(v).append("\n"));
        // Overwrite (never append) — regeneration is idempotent by design.
        Files.writeString(out, sb.toString());
    }

    private void registerModuleInRootPom(Path rootPom, String modulePath) {
        try {
            String content = Files.readString(rootPom);
            String moduleTag = "<module>" + modulePath + "</module>";
            if (content.contains(moduleTag)) return;
            String updated = content.replaceFirst(
                    "(\\s+</modules>)",
                    "\n        " + moduleTag + "$1"
            );
            if (!updated.equals(content)) {
                Files.writeString(rootPom, updated, StandardOpenOption.TRUNCATE_EXISTING);
                getLog().info("Registered " + modulePath + " in root pom <modules>");
            }
        } catch (IOException e) {
            getLog().warn("Failed to update root pom modules list: " + e.getMessage());
        }
    }

    private Map<String, Object> readMavenCoords(Path pomFile) throws MojoExecutionException {
        try {
            String content = Files.readString(pomFile);
            Map<String, Object> coords = new LinkedHashMap<>();
            coords.put("groupId", extractTag(content, "groupId", true));
            coords.put("artifactId", extractTag(content, "artifactId", true));
            coords.put("version", extractTag(content, "version", true));
            return coords;
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to read " + pomFile, e);
        }
    }

    /**
     * Naive but sufficient for pom.xml: returns the FIRST direct child tag content after the &lt;project&gt; root
     * (skipping any nested tags inside &lt;parent&gt;).
     */
    private String extractTag(String pom, String tag, boolean skipParentBlock) {
        String workingPom = pom;
        if (skipParentBlock) {
            int parentStart = workingPom.indexOf("<parent>");
            int parentEnd = workingPom.indexOf("</parent>");
            if (parentStart >= 0 && parentEnd > parentStart) {
                workingPom = workingPom.substring(0, parentStart) + workingPom.substring(parentEnd + "</parent>".length());
            }
        }
        String open = "<" + tag + ">";
        String close = "</" + tag + ">";
        int i = workingPom.indexOf(open);
        if (i < 0) return null;
        int j = workingPom.indexOf(close, i + open.length());
        if (j < 0) return null;
        return workingPom.substring(i + open.length(), j).trim();
    }

    private Path findRepoRoot() {
        MavenProject p = project;
        while (p.getParent() != null && p.getParent().getBasedir() != null) {
            p = p.getParent();
        }
        return p.getBasedir().toPath();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object o) {
        return (o instanceof Map<?, ?> m) ? (Map<String, Object>) m : null;
    }

    private String str(Object o, String fallback) {
        return o == null ? fallback : o.toString();
    }

    private String toClassName(String hyphenated) {
        StringBuilder sb = new StringBuilder();
        for (String part : hyphenated.split("[-_]")) {
            if (part.isEmpty()) continue;
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }

    private Map<String, Object> loadYaml(Path file) throws MojoExecutionException {
        try (InputStream in = Files.newInputStream(file)) {
            Object doc = new Yaml().load(in);
            if (!(doc instanceof Map)) {
                throw new MojoExecutionException("Expected YAML map at root of " + file);
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) doc;
            return map;
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to read " + file, e);
        }
    }
}
