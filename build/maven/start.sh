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
-Dserver.port=${SERVER_PORT:-8094} \
-Dmanagement.endpoints.web.base-path=${MANAGEMENT_ENDPOINTS_WEB_BASE_PATH:-/} \
-Dserver.context-path=${SERVER_CONTEXT_PATH:-/egov-mdms-service} \
-Dserver.servlet.context-path=${SERVER_SERVLET_CONTEXT_PATH:-/egov-mdms-service} \
-Dapp.timezone=${APP_TIMEZONE:-UTC} \
-Degov.mdms.conf.path=${EGOV_MDMS_CONF_PATH:-/D:/egov/mdms/IN_UK_MDMSDATA} \
-Degov.mdms.stopOnAnyConfigError=${EGOV_MDMS_STOP_ON_ANY_CONFIG_ERROR:-true} \
-Dotel.exporter.otlp.endpoint=${OTEL_EXPORTER_OTLP_ENDPOINT:-http://my-release-signoz-otel-collector.platform.svc.cluster.local:4317} \
-Dotel.resource.attributes=${OTEL_RESOURCE_ATTRIBUTES:-service.name=egov-mdms-service} \
-Dotel.traces.exporter=${OTEL_TRACES_EXPORTER:-otlp} \
-Dotel.metrics.exporter=${OTEL_METRICS_EXPORTER:-otlp} \
-Dotel.logs.exporter=${OTEL_LOGS_EXPORTER:-otlp}"

# Run the Java application
exec java ${java_debug_args} ${JAVA_OPTS} ${JAVA_ARGS} -jar /opt/egov/*.jar
