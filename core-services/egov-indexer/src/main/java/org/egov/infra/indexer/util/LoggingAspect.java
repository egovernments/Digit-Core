package org.egov.infra.indexer.util;
import org.springframework.beans.factory.annotation.Value;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import static org.egov.infra.indexer.util.IndexerConstants.TENANTID_MDC_STRING;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Value("${egov.statelevel.tenantId}")
    private String stateLevelTenantId;

    @Before("execution(* org.egov.infra.indexer.consumer.*.*(..))")
    public void beforeMethodCall() {

        // Adding in MDC so that tracer can add it in header
        // Code to run before a method is executed
        MDC.put(TENANTID_MDC_STRING, stateLevelTenantId );
        log.info("Before method execution adding tenantId in header via MDC");
    }


}
