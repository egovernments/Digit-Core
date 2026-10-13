FROM eclipse-temurin:25-jdk-alpine AS build
ARG WORK_DIR
ARG nexusUsername
ARG nexusPassword
WORKDIR /app

RUN apk add --no-cache maven

COPY ${WORK_DIR}/pom.xml ./pom.xml
COPY ${WORK_DIR}/settings.xml ./settings.xml
COPY ${WORK_DIR}/src ./src

RUN mvn -B -f /app/pom.xml deploy -Dmaven.test.skip=true -s settings.xml \
    -Dnexus.user=${nexusUsername} -Dnexus.password=${nexusPassword}

FROM scratch
