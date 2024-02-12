FROM egovio/amazoncorretto:17-alpine3.19 AS build
ARG WORK_DIR
ARG nexusUsername
ARG nexusPassword
WORKDIR /app

# Install Maven
RUN apk add --no-cache maven

# copy the project files
COPY ${WORK_DIR}/pom.xml ./pom.xml
COPY ${WORK_DIR}/settings.xml ./settings.xml
# COPY build/maven/settings.xml ./settings.xml
# COPY build/maven/start.sh ./start.sh
COPY ${WORK_DIR}/src ./src
# not useful for stateless builds
# RUN mvn -B dependency:go-offline
RUN cd ${WORK_DIR} \
    && mvn -B -f /app/pom.xml test verify deploy -s settings.xml  \
    -Dnexus.user=${nexusUsername} -Dnexus.password=${nexusPassword}
FROM scratch