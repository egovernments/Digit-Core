# ──────────────── BUILD STAGE ────────────────
FROM maven:3.9-eclipse-temurin-17 AS build
ARG WORK_DIR
WORKDIR /app

# Copy project files
COPY ${WORK_DIR}/pom.xml ./pom.xml
COPY ${WORK_DIR}/src ./src

# Build the project
RUN mvn -B -f /app/pom.xml package -DskipTests

# ─────────────── RUNTIME STAGE ───────────────
FROM gcr.io/distroless/java17-debian12:nonroot

WORKDIR /opt/egov

# Copy artifact from the build stage
COPY --from=build /app/target/*.jar ./application.jar

# -XX:-UseBASE64Intrinsics disables the AVX-512 Base64 intrinsic affected by
# CVE-2026-114938 (Java4Shell). Interim mitigation until the runtime image
# ships OpenJDK 17.0.21.1 or later; remove once the base image is patched.
ENTRYPOINT ["java", "-XX:-UseBASE64Intrinsics", "-jar", "application.jar"]
