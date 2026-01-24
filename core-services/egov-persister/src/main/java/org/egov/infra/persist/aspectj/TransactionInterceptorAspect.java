package org.egov.infra.persist.aspectj;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class TransactionInterceptorAspect {

    @Around("@annotation(org.springframework.transaction.annotation.Transactional)")
    public Object aroundTransactional(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            Object result = joinPoint.proceed();
            log.info("Transactional method succeeded: {}", joinPoint.getSignature());
            return result;
        } catch (Exception e) {
            log.error("Transactional method failed: {}", joinPoint.getSignature());
            throw new ListenerExecutionFailedException(e.getMessage(), e);
        }
    }
}
