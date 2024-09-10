#!/bin/sh

# Set default Java options if not set
if [[ -z "${JAVA_OPTS}" ]];then
    export JAVA_OPTS="-Xmx64m -Xms64m"
fi

# Enable Java debugging if the flag is set
if [ x"${JAVA_ENABLE_DEBUG}" != x ] && [ "${JAVA_ENABLE_DEBUG}" != "false" ]; then
    java_debug_args="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=${JAVA_DEBUG_PORT:-5005}"
fi

# Set OpenTelemetry and application-specific settings from environment variables, with defaults
export JAVA_OPTS="${JAVA_OPTS} \
-javaagent:/opt/egov/opentelemetry-javaagent.jar \
-Dotel.exporter.otlp.endpoint=${OTEL_EXPORTER_OTLP_ENDPOINT:-http://my-release-signoz-otel-collector.platform.svc.cluster.local:4317} \
-Dotel.traces.exporter=${OTEL_TRACES_EXPORTER:-otlp} \
-Dotel.metrics.exporter=${OTEL_METRICS_EXPORTER:-otlp} \
-Dotel.logs.exporter=${OTEL_LOGS_EXPORTER:-otlp}"

# Run the Java application
exec java ${java_debug_args} ${JAVA_OPTS} ${JAVA_ARGS} -jar /opt/egov/*.jar
