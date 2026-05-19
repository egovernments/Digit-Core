package org.egov.domain.service;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.egov.persistence.repository.DefaultDataJpaRepository;
import org.egov.web.contract.DefaultDataRequest;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
@Slf4j
public class DefaultDataService {

    private final DefaultDataJpaRepository defaultDataJpaRepository;

    public DefaultDataService(DefaultDataJpaRepository defaultDataJpaRepository) {
        this.defaultDataJpaRepository = defaultDataJpaRepository;
    }

    public void create(@Valid DefaultDataRequest defaultDataRequest) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        boolean sync = Boolean.TRUE.equals(defaultDataRequest.getMigrationSync());
        int count;
        if (sync) {
            count = defaultDataJpaRepository.syncMessageDefinitions(defaultDataRequest.getDefaultTenantId(), defaultDataRequest.getTargetTenantId(), now, defaultDataRequest.getLocale(), defaultDataRequest.getModules());
            log.info("Sync upserted {} records for tenant={} locale={}", count, defaultDataRequest.getTargetTenantId(), defaultDataRequest.getLocale());
        } else {
            count = defaultDataJpaRepository.copyMessageDefinitions(defaultDataRequest.getDefaultTenantId(), defaultDataRequest.getTargetTenantId(), now, defaultDataRequest.getLocale(), defaultDataRequest.getModules());
            log.info("Inserted {} records for tenant={} locale={}", count, defaultDataRequest.getTargetTenantId(), defaultDataRequest.getLocale());
        }
    }
}
