package digit.config;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ApplicationConfig {

    @Value("${kafka.topics.create.tenant}")
    private String createTopic;

    @Value("${kafka.topics.create.sub.tenant}")
    private String createSubTenantTopic;

    @Value("${kafka.topics.create.tenant.config}")
    private String createConfigTopic;

    @Value("${kafka.topics.update.tenant.config}")
    private String updateConfigTopic;

    @Value("${kafka.topics.update.tenant}")
    private String updateTopic;

    @Value("${mdms.default.offset}")
    private Integer defaultOffset;

    @Value("${mdms.default.limit}")
    private Integer defaultLimit;

    @Value("${egov.user.host}${egov.user.search.path}")
    private String UserSearchURI;

    @Value("${kafka.topics.notification.mail.name}")
    private String emailTopic;

    @Value("${egov.mdms.host}${egov.mdms.default.data.create.endpoint}")
    private String mdmsDefaultDataCreateURI;

    @Value("#{'${default.mdms.create.list}'.split(',')}")
    private List<String> defaultMdmsSchemaList;

    // User Config
    @Value("${egov.user.host}")
    private String userHost;

    @Value("${egov.user.context.path}")
    private String userContextPath;

    @Value("${egov.user.create.path}")
    private String userCreateEndpoint;

    @Value("${egov.user.search.path}")
    private String userSearchEndpoint;

    @Value("${egov.user.update.path}")
    private String userUpdateEndpoint;


    //Idgen Config
    @Value("${egov.idgen.host}")
    private String idGenHost;

    @Value("${egov.idgen.path}")
    private String idGenPath;


    //Workflow Config
    @Value("${egov.workflow.host}")
    private String wfHost;

    @Value("${egov.workflow.transition.path}")
    private String wfTransitionPath;

    @Value("${egov.workflow.businessservice.search.path}")
    private String wfBusinessServiceSearchPath;

    @Value("${egov.workflow.processinstance.search.path}")
    private String wfProcessInstanceSearchPath;


    //MDMS
    @Value("${egov.mdms.host}")
    private String mdmsHost;

    @Value("${egov.mdms.search.endpoint}")
    private String mdmsEndPoint;


    //HRMS
    @Value("${egov.hrms.host}")
    private String hrmsHost;

    @Value("${egov.hrms.search.endpoint}")
    private String hrmsEndPoint;


    //URLShortening
    @Value("${egov.url.shortner.host}")
    private String urlShortnerHost;

    @Value("${egov.url.shortner.endpoint}")
    private String urlShortnerEndpoint;


    //SMSNotification
    @Value("${egov.sms.notification.topic}")
    private String smsNotificationTopic;

    // User OTP Configuration
    @Value("${egov.user.otp.host}")
    private String userOtpHost;

    @Value("${egov.user.otp.send.endpoint}")
    private String userOtpSendEndpoint;

}
