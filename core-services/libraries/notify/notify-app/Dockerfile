FROM maven:3.9-eclipse-temurin-25 AS builder
WORKDIR /build
COPY pom.xml .
COPY notify-spi/pom.xml notify-spi/
COPY notify-app/pom.xml notify-app/
RUN mvn dependency:go-offline -B -q
COPY notify-spi/src notify-spi/src
COPY notify-app/src notify-app/src
RUN mvn package -pl notify-app -am -DskipTests -B -q

FROM eclipse-temurin:25-jre-alpine
RUN addgroup -S notify && adduser -S notify -G notify
WORKDIR /app
COPY --from=builder /build/notify-app/target/notify-app-*.jar app.jar
RUN mkdir /providers && chown notify:notify /providers
USER notify
EXPOSE 8080
ENTRYPOINT ["java", "--enable-preview", "-jar", "/app/app.jar"]
