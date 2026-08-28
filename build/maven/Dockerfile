# syntax=docker/dockerfile:1.7

# ──────────────── BUILD STAGE ────────────────
FROM amazoncorretto:25 AS build
ARG WORK_DIR
WORKDIR /app

# Install Maven
RUN dnf install -y maven && dnf clean all

# Copy project files
COPY ${WORK_DIR}/pom.xml ./pom.xml
COPY ${WORK_DIR}/src ./src

# Build the project
RUN mvn -B -DskipTests -f /app/pom.xml package spring-boot:repackage \
  && rm -f /app/target/original-*.jar

# ─────────────── RUNTIME STAGE ───────────────
FROM amazoncorretto:25

WORKDIR /opt/egov

# Copy artifact from the build stage
COPY --from=build /app/target/*.jar /opt/egov/

# Create start script in-image and make it executable
RUN printf '%s\n' \
  '#!/bin/sh' \
  '' \
  'if [ -z "${JAVA_OPTS}" ]; then' \
  '  export JAVA_OPTS="-Xmx64m -Xms64m"' \
  'fi' \
  '' \
  'if [ x"${JAVA_ENABLE_DEBUG}" != x ] && [ "${JAVA_ENABLE_DEBUG}" != "false" ]; then' \
  '  java_debug_args="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=${JAVA_DEBUG_PORT:-5005}"' \
  'fi' \
  '' \
  'exec java ${java_debug_args} ${JAVA_OPTS} ${JAVA_ARGS} -jar /opt/egov/*.jar' \
  > /opt/egov/start.sh \
  && chmod +x /opt/egov/start.sh

# Verify architecture inside the container
RUN uname -m

CMD ["/opt/egov/start.sh"]
