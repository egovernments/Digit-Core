# JDK 8→21 hybrid: compile with JDK 8, run on JDK 21.
# Use this for services on Spring Boot 1.5/2.x that cannot be recompiled
# for JDK 17+ but benefit from JDK 21's cgroup v2 fix and memory improvements.
#
# JDK 21 fixes cgroup v2 CPU detection (JDK-8281181), auto-sizes heap/threads
# from container limits, and runs Java 8 bytecode natively.
#
# Spring Boot 1.5 needs two runtime patches for JDK 21:
#   1. JAXB API + runtime (removed from JDK 11)
#   2. --add-opens flags for internal JDK module access

# ──────────────── BUILD STAGE ────────────────
FROM egovio/maven:3.9.6-amazoncorretto-8-debian AS build
ARG WORK_DIR
WORKDIR /app

COPY ${WORK_DIR}/pom.xml       ./pom.xml
COPY build/maven-java8-jdk21/start.sh ./start.sh
COPY ${WORK_DIR}/src           ./src

RUN mvn -B -f pom.xml package -DskipTests

# ──────────── JAXB PATCH STAGE ─────────────
FROM eclipse-temurin:21-jdk-alpine AS patcher
RUN apk add --no-cache curl
WORKDIR /patch

COPY --from=build /app/target/*.jar ./app.jar

# Download JAXB API + implementation (removed from JDK 11+)
RUN curl -sL -o jaxb-api.jar https://repo1.maven.org/maven2/javax/xml/bind/jaxb-api/2.3.1/jaxb-api-2.3.1.jar && \
    curl -sL -o javax.activation.jar https://repo1.maven.org/maven2/javax/activation/javax.activation-api/1.2.0/javax.activation-api-1.2.0.jar && \
    curl -sL -o jaxb-runtime.jar https://repo1.maven.org/maven2/org/glassfish/jaxb/jaxb-runtime/2.3.9/jaxb-runtime-2.3.9.jar && \
    curl -sL -o txw2.jar https://repo1.maven.org/maven2/org/glassfish/jaxb/txw2/2.3.9/txw2-2.3.9.jar && \
    curl -sL -o istack-commons-runtime.jar https://repo1.maven.org/maven2/com/sun/istack/istack-commons-runtime/3.0.12/istack-commons-runtime-3.0.12.jar && \
    curl -sL -o stax-ex.jar https://repo1.maven.org/maven2/org/jvnet/staxex/stax-ex/1.8.1/stax-ex-1.8.1.jar

# Inject JAXB jars into the fat JAR (jar uf0 preserves STORED entries)
RUN mkdir -p BOOT-INF/lib && \
    cp jaxb-api.jar javax.activation.jar jaxb-runtime.jar txw2.jar \
       istack-commons-runtime.jar stax-ex.jar BOOT-INF/lib/ && \
    jar uf0 app.jar \
      BOOT-INF/lib/jaxb-api.jar \
      BOOT-INF/lib/javax.activation.jar \
      BOOT-INF/lib/jaxb-runtime.jar \
      BOOT-INF/lib/txw2.jar \
      BOOT-INF/lib/istack-commons-runtime.jar \
      BOOT-INF/lib/stax-ex.jar

# ─────────────── RUNTIME STAGE ───────────────
FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache dos2unix

WORKDIR /opt/egov

COPY --from=patcher /patch/app.jar /opt/egov/app.jar
COPY --from=build /app/start.sh /opt/egov/start.sh

# CDS pre-dump: generate classlist then shared archive for faster startup.
RUN timeout 30 java \
      --add-opens java.base/java.lang=ALL-UNNAMED \
      --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
      --add-opens java.base/java.util=ALL-UNNAMED \
      -XX:DumpLoadedClassList=/opt/egov/app.classlist \
      -jar /opt/egov/app.jar 2>/dev/null; true
RUN java \
      --add-opens java.base/java.lang=ALL-UNNAMED \
      --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
      --add-opens java.base/java.util=ALL-UNNAMED \
      -Xshare:dump \
      -XX:SharedClassListFile=/opt/egov/app.classlist \
      -XX:SharedArchiveFile=/opt/egov/app-cds.jsa \
      -jar /opt/egov/app.jar 2>/dev/null; true

RUN dos2unix /opt/egov/start.sh && chmod +x /opt/egov/start.sh

CMD ["/opt/egov/start.sh"]
