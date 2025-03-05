FROM --platform=linux/arm64 maven:3.9.6-amazoncorretto-17 AS build
ARG WORK_DIR
WORKDIR /app

# Copy project files
COPY ${WORK_DIR}/pom.xml ./pom.xml
COPY build/maven/start.sh ./start.sh
COPY ${WORK_DIR}/src ./src

# Build the project
RUN mvn -B -f /app/pom.xml package

# Runtime image - Use an ARM64-supported Amazon Corretto image
FROM --platform=linux/arm64 amazoncorretto:17-alpine

WORKDIR /opt/egov

# Copy artifacts from the build stage
COPY --from=build /app/target/*.jar /app/start.sh /opt/egov/

# Make start script executable
RUN chmod +x /opt/egov/start.sh

CMD ["/opt/egov/start.sh"]
