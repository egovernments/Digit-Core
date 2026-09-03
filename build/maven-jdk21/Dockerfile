# JDK 21 runtime variant of build/maven/Dockerfile.
# Use this for services on Spring Boot 3.x compiled with Java 17+.
# Services on Spring Boot 1.5/2.x must continue using build/maven/Dockerfile.
#
# JDK 21 fixes cgroup v2 CPU detection (JDK-8281181), auto-sizes heap/threads
# from container limits, and runs Java 17 bytecode natively (no recompilation).

FROM egovio/maven:3.9.6-amazoncorretto-17 AS build
ARG WORK_DIR
WORKDIR /app

# Copy project files
COPY ${WORK_DIR}/pom.xml ./pom.xml
COPY build/maven-jdk21/start.sh ./start.sh
COPY ${WORK_DIR}/src ./src

# Build the project
RUN mvn -B -f /app/pom.xml package

# Runtime image — JDK 21 on Alpine
FROM eclipse-temurin:21-jre-alpine

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
