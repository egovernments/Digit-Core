#!/bin/sh

if [[ -z "${JAVA_OPTS}" ]];then
    export JAVA_OPTS="-Xmx64m -Xms64m"
fi

if [ x"${JAVA_ENABLE_DEBUG}" != x ] && [ "${JAVA_ENABLE_DEBUG}" != "false" ]; then
    java_debug_args="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=${JAVA_DEBUG_PORT:-5005}"
fi

export JAVA_OPTS="${JAVA_OPTS} -javaagent:/opt/egov/opentelemetry-javaagent.jar"

exec java ${java_debug_args} ${JAVA_OPTS} ${JAVA_ARGS}  -jar /opt/egov/*.jar