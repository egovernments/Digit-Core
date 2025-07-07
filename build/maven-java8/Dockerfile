# ──────────────── BUILD STAGE ────────────────
FROM maven:3.9.6-amazoncorretto-8-debian AS build
ARG WORK_DIR
WORKDIR /app

# Copy POM, entrypoint script, and source
COPY ${WORK_DIR}/pom.xml       ./pom.xml
COPY build/maven-java8/start.sh ./start.sh
COPY ${WORK_DIR}/src           ./src

# Build the app (skip tests for speed)
RUN mvn -B -f pom.xml package -DskipTests


# ─────────────── RUNTIME STAGE ───────────────
FROM openjdk:8-jdk-alpine AS runtime
WORKDIR /opt/egov

# Pull in the fat JAR and our startup script
COPY --from=build /app/target/*.jar ./application.jar
COPY --from=build /app/start.sh    ./start.sh

RUN chmod +x ./start.sh

# Override any Alpine tini entrypoint and launch via shell
ENTRYPOINT ["sh", "./start.sh"]
