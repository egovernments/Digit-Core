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

ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v1.25.0/opentelemetry-javaagent.jar /opt/egov/opentelemetry-javaagent.jar

RUN chmod +x /opt/egov/start.sh

CMD ["/opt/egov/start.sh"]