package digit.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;

import digit.config.ApplicationConfig;
import digit.util.MdmsUtil;
import digit.util.OtpUtil;
import digit.util.UserUtil;
import digit.web.models.DefaultMasterDataRequest;
import digit.web.models.Tenant;
import digit.web.models.TenantRequest;
import lombok.extern.slf4j.Slf4j;
import org.egov.mdms.model.MDMSCreateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Objects;

@Slf4j
@Component
public class TenantConsumer {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private UserUtil userUtil;

    @Autowired
    private OtpUtil otpUtil;

    @Autowired
    private MdmsUtil mdmsUtil;

    @Autowired
    private ApplicationConfig applicationConfig;

    // @KafkaListener(topics = {"${kafka.topics.create.tenant}"})
    public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        TenantRequest tenantRequest = mapper.convertValue(record, TenantRequest.class);

        // create user only for root tenant
        if (Objects.isNull(tenantRequest.getTenant().getParentId())){

            log.info("Configuring Tenant: {}",tenantRequest.getTenant().getCode());

            mdmsUtil.createMdmsData(new DefaultMasterDataRequest()
                    .builder()
                    .requestInfo(tenantRequest.getRequestInfo())
                    .schemaCodes(applicationConfig.getDefaultMdmsSchemaList())
                    .targetTenantId(tenantRequest.getTenant().getCode())
                    .build());

            userUtil.createUser(tenantRequest);
            otpUtil.sendOtp(tenantRequest);
        }


        // TODO: Trigger email/sms OTP here
    }

}
