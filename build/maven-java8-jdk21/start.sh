#!/bin/sh

# JDK 21 auto-sizes heap from cgroup limits — no default -Xms/-Xmx needed.
# Set JAVA_OPTS or JAVA_TOOL_OPTIONS via container environment if tuning is required.

# CDS: use shared archive if it exists (pre-baked at image build time)
CDS_OPTS=""
if [ -f /opt/egov/app-cds.jsa ]; then
    CDS_OPTS="-XX:SharedArchiveFile=/opt/egov/app-cds.jsa"
fi

# Spring Boot 1.5 uses internal JDK APIs via reflection.
# These --add-opens flags are required for JDK 21 compatibility.
ADD_OPENS="--add-opens java.base/java.lang=ALL-UNNAMED"
ADD_OPENS="$ADD_OPENS --add-opens java.base/java.lang.reflect=ALL-UNNAMED"
ADD_OPENS="$ADD_OPENS --add-opens java.base/java.util=ALL-UNNAMED"
# Tomcat clears ObjectStreamClass caches via reflection on context stop.
ADD_OPENS="$ADD_OPENS --add-opens java.base/java.io=ALL-UNNAMED"

if [ x"${JAVA_ENABLE_DEBUG}" != x ] && [ "${JAVA_ENABLE_DEBUG}" != "false" ]; then
    java_debug_args="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=${JAVA_DEBUG_PORT:-5005}"
fi

exec java ${java_debug_args} ${ADD_OPENS} ${JAVA_OPTS} ${CDS_OPTS} ${JAVA_ARGS} -jar /opt/egov/app.jar
