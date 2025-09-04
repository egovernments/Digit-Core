#docker buildx command to build arm64 image, docker buildx build \\n  --platform linux/amd64,linux/arm64 \\n  -f core-services/keycloak-spi/Dockerfile \\n  -t egovio/keycloak:keycloak-spi-multiarch \\n  --push \\n  .\n


FROM quay.io/keycloak/keycloak:25.0.1 AS builder

WORKDIR /opt/keycloak

# Copy custom provider JARs from root context
COPY --chown=keycloak:keycloak core-services/keycloak-spi/providers/ /opt/keycloak/providers/

# Run Keycloak build to optimize the server with custom providers
RUN /opt/keycloak/bin/kc.sh build

FROM quay.io/keycloak/keycloak:25.0.1

COPY --from=builder /opt/keycloak/ /opt/keycloak/

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
CMD ["start-dev"]