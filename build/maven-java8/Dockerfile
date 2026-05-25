# ──────────────── BUILD STAGE ────────────────
FROM maven:3.9-eclipse-temurin-8 AS build
ARG WORK_DIR
WORKDIR /app

# Copy POM, entrypoint script, and source
COPY ${WORK_DIR}/pom.xml       ./pom.xml
COPY build/maven-java8/start.sh ./start.sh
COPY ${WORK_DIR}/src           ./src

# Build the app (skip tests for speed)
RUN mvn -B -f pom.xml package -DskipTests


# ─────────────── RUNTIME STAGE ───────────────
FROM eclipse-temurin:8-jre AS runtime
WORKDIR /opt/egov

# Pull in the fat JAR and our startup script
COPY --from=build /app/target/*.jar ./application.jar
COPY --from=build /app/start.sh    ./start.sh

RUN chmod +x ./start.sh

ENTRYPOINT ["sh", "./start.sh"]

