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
        int insertedCount = defaultDataJpaRepository.copyMessageDefinitions(defaultDataRequest.getDefaultTenantId(), defaultDataRequest.getTargetTenantId(), new Timestamp(System.currentTimeMillis()), defaultDataRequest.getLocale(), defaultDataRequest.getModules());
        log.info("Number of records inserted: {}", insertedCount);
    }
}
