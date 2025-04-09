FROM quay.io/keycloak/keycloak:25.0.1 AS builder

WORKDIR /opt/keycloak

# Copy custom provider JARs
COPY --chown=keycloak:keycloak providers/ /opt/keycloak/providers/

# Run Keycloak build to optimize the server with custom providers
RUN /opt/keycloak/bin/kc.sh build

FROM quay.io/keycloak/keycloak:25.0.1

COPY --from=builder /opt/keycloak/ /opt/keycloak/

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]

CMD ["start-dev"]
