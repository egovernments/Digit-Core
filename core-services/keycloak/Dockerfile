#docker buildx command to build arm64 image, docker buildx build \\n  --platform linux/amd64,linux/arm64 \\n  -f core-services/keycloak-spi/Dockerfile \\n  -t egovio/keycloak:keycloak-spi-multiarch \\n  --push \\n  .\n


FROM quay.io/keycloak/keycloak:25.0.1 AS builder

WORKDIR /opt/keycloak

# Copy custom provider JARs from root context
COPY --chown=keycloak:keycloak core-services/keycloak/providers/ /opt/keycloak/providers/

# Copy custom login themes (e.g. "digit") from root context
COPY --chown=keycloak:keycloak core-services/keycloak/themes/ /opt/keycloak/themes/

# Run Keycloak build to optimize the server with custom providers
RUN /opt/keycloak/bin/kc.sh build

FROM quay.io/keycloak/keycloak:25.0.1

COPY --from=builder /opt/keycloak/ /opt/keycloak/

# Java4Shell (CVE-2026-114938) stopgap: the bundled JDK (21.0.3) is affected, so the vulnerable
# Base64 intrinsic is switched off. kc.sh appends JAVA_OPTS_APPEND to its JVM options.
# Remove this once the base image carries a patched JDK (quay.io/keycloak/keycloak:26.8.2-3 or later).
ENV JAVA_OPTS_APPEND="-XX:-UseBASE64Intrinsics"

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
CMD ["start-dev"]
