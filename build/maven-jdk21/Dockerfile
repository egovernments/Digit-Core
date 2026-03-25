# JDK 25 runtime variant of build/maven/Dockerfile.
# Use this for services on Spring Boot 3.x compiled with Java 25+.
# Services on Spring Boot 1.5/2.x must continue using build/maven-java8-jdk25/Dockerfile.
#
# JDK 25 LTS: virtual threads, generational ZGC, cgroup v2 support,
# auto-sizes heap/threads from container limits.

FROM maven:3.9.9-eclipse-temurin-25-alpine AS build
ARG WORK_DIR
WORKDIR /app

# Copy project files
COPY ${WORK_DIR}/pom.xml ./pom.xml
COPY build/maven-jdk21/start.sh ./start.sh
COPY ${WORK_DIR}/src ./src

# Build the project
RUN mvn -B -f /app/pom.xml package

# Runtime image — JDK 25 LTS on Alpine
FROM eclipse-temurin:25-jre-alpine

RUN apk add --no-cache dos2unix

WORKDIR /opt/egov

# Copy artifacts from the build stage
COPY --from=build /app/target/*.jar /opt/egov/app.jar
COPY --from=build /app/start.sh /opt/egov/start.sh

# CDS pre-dump: generate classlist then shared archive for faster startup.
# The app will attempt to start (no DB/Kafka available) — timeout kills it
# after 30s, which is enough to load all classes for the classlist.
RUN timeout 30 java -XX:DumpLoadedClassList=/opt/egov/app.classlist \
      -jar /opt/egov/app.jar 2>/dev/null; true
RUN java -Xshare:dump \
      -XX:SharedClassListFile=/opt/egov/app.classlist \
      -XX:SharedArchiveFile=/opt/egov/app-cds.jsa \
      -jar /opt/egov/app.jar 2>/dev/null; true

# Ensure the start script has correct line endings and is executable
RUN dos2unix /opt/egov/start.sh && chmod +x /opt/egov/start.sh

CMD ["/opt/egov/start.sh"]
