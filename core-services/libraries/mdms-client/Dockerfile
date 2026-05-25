FROM maven:3.9-eclipse-temurin-17 AS build
ARG WORK_DIR
ARG nexusUsername
ARG nexusPassword
WORKDIR /app

# copy the project files
COPY ${WORK_DIR}/pom.xml ./pom.xml
COPY ${WORK_DIR}/settings.xml ./settings.xml
COPY ${WORK_DIR}/src ./src

RUN mvn -B -f /app/pom.xml test verify deploy -s settings.xml \
    -Dnexus.user=${nexusUsername} -Dnexus.password=${nexusPassword}
FROM scratch
