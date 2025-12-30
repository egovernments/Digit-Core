# JVM-based Docker image for Spring Boot 4
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN apk add --no-cache maven && \
    mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/egov-idgen-*.jar /app/egov-idgen.jar

EXPOSE 8088

ENTRYPOINT ["java", "-jar", "/app/egov-idgen.jar"]
