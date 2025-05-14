<<<<<<< HEAD
#FROM egovio/alpine-maven-builder-jdk-8:1-master-NA-6036091e AS build
FROM egovio/amazoncorretto:17-alpine3.19 AS build
ARG WORK_DIR
WORKDIR /app

# Install Maven
RUN apk add --no-cache maven

# copy the project files
COPY ${WORK_DIR}/pom.xml ./pom.xml
COPY build/maven/start.sh ./start.sh

# not useful for stateless builds
# RUN mvn -B dependency:go-offline

COPY ${WORK_DIR}/src ./src
RUN mvn -B -f /app/pom.xml package


# Create runtime image
#FROM egovio/8-openjdk-alpine
FROM egovio/amazoncorretto:17-alpine3.19


WORKDIR /opt/egov

COPY --from=build /app/target/*.jar /app/start.sh /opt/egov/

RUN chmod +x /opt/egov/start.sh
=======
FROM maven:3.9.6-amazoncorretto-17 AS build
ARG WORK_DIR
WORKDIR /app

# Copy project files
COPY ${WORK_DIR}/pom.xml ./pom.xml
COPY build/maven/start.sh ./start.sh
COPY ${WORK_DIR}/src ./src

# Build the project
RUN mvn -B -f /app/pom.xml package

# Runtime image – using a multi-arch base image
FROM amazoncorretto:17-alpine

WORKDIR /opt/egov

# Copy artifacts from the build stage
COPY --from=build /app/target/*.jar /app/start.sh /opt/egov/

# Ensure the start script has correct line endings and is executable
RUN dos2unix /opt/egov/start.sh && chmod +x /opt/egov/start.sh

# Verify architecture inside the container
RUN uname -m
>>>>>>> WIP: changes to Dockerfile and workflow

CMD ["/opt/egov/start.sh"]