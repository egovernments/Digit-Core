package org.egov.handler.kafka;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.User;
import org.egov.handler.config.ServiceConfiguration;
import org.egov.handler.service.DataHandlerService;
import org.egov.handler.util.LocalizationUtil;
import org.egov.handler.util.OtpUtil;
import org.egov.handler.util.UserUtil;
import org.egov.handler.web.models.DefaultDataRequest;
import org.egov.handler.web.models.DefaultLocalizationDataRequest;
import org.egov.handler.web.models.TenantRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;

@Slf4j
@Component
public class TenantConsumer {

    private final ObjectMapper mapper;

    private final DataHandlerService dataHandlerService;

    private final UserUtil userUtil;

    private final OtpUtil otpUtil;

    private final ServiceConfiguration serviceConfig;

    private LocalizationUtil localizationUtil;

    @Autowired
    public TenantConsumer(ObjectMapper mapper, DataHandlerService dataHandlerService, UserUtil userUtil, OtpUtil otpUtil, ServiceConfiguration serviceConfig, LocalizationUtil localizationUtil) {
        this.mapper = mapper;
        this.dataHandlerService = dataHandlerService;
        this.userUtil = userUtil;
        this.otpUtil = otpUtil;
        this.serviceConfig = serviceConfig;
        this.localizationUtil = localizationUtil;
    }

    @KafkaListener(topics = {"${kafka.topics.create.tenant}"})
    public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        TenantRequest tenantRequest = mapper.convertValue(record, TenantRequest.class);

        // Setting userInfo with UUID
        User userInfo = User.builder()
                .uuid("40dceade-992d-4a8f-8243-19dda76a4171")
                .build();
        tenantRequest.getRequestInfo().setUserInfo(userInfo);


        DefaultLocalizationDataRequest defaultLocalizationDataRequest = DefaultLocalizationDataRequest.builder()
                .requestInfo(tenantRequest.getRequestInfo())
                .targetTenantId(tenantRequest.getTenant().getCode())
                .locale(serviceConfig.getDefaultLocalizationLocale())
                .modules(Collections.singletonList("rainmaker-common"))
                .build();

        localizationUtil.upsertLocalization(defaultLocalizationDataRequest, tenantRequest);

        // create user only for root tenant
        if (Objects.isNull(tenantRequest.getTenant().getParentId())) {
            log.info("Configuring Tenant: {}", tenantRequest.getTenant().getCode());

            DefaultDataRequest defaultDataRequest = DefaultDataRequest.builder()
                    .requestInfo(tenantRequest.getRequestInfo())
                    .targetTenantId(tenantRequest.getTenant().getCode())
                    .schemaCodes(serviceConfig.getDefaultMdmsSchemaList())
                    .onlySchemas(Boolean.FALSE)
                    .locale(serviceConfig.getDefaultLocalizationLocale())
                    .modules(serviceConfig.getDefaultLocalizationModuleList())
                    .build();

            dataHandlerService.createDefaultData(defaultDataRequest);
            dataHandlerService.createTenantConfig(tenantRequest);

            userUtil.createUser(tenantRequest);
            otpUtil.sendOtp(tenantRequest);
        }
    }
}
