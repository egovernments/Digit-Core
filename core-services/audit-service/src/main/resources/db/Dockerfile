# Use an ARM64-compatible Flyway image
FROM flyway/flyway:10.7.1-alpine

COPY ./migration/main /flyway/sql
COPY migrate.sh /usr/bin/migrate.sh

# Ensure script has correct line endings and is executable
RUN apk add --no-cache dos2unix && dos2unix /usr/bin/migrate.sh && chmod +x /usr/bin/migrate.sh

ENTRYPOINT ["/usr/bin/migrate.sh"]
