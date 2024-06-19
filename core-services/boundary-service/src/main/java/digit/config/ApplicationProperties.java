package digit.config;

import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ApplicationProperties {


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

    // Kafka Config
    @Value("${kafka.topics.create.boundary}")
    private String createBoundaryTopic;

    @Value("${kafka.topics.update.boundary}")
    private String updateBoundaryTopic;

    @Value("${kafka.topics.create.boundary.hierarchy}")
    private String createBoundaryHierarchyTopic;

    @Value("${kafka.topics.update.boundary.hierarchy}")
    private String updateBoundaryHierarchyTopic;

    @Value("${kafka.topics.create.boundary.relationship}")
    private String createBoundaryRelationshipTopic;

    @Value("${kafka.topics.update.boundary.relationship}")
    private String updateBoundaryRelationshipTopic;

    @Value("${boundary.default.offset}")
    private Integer defaultOffset;

    @Value("${boundary.default.limit}")
    private Integer defaultLimit;

    @Value("${boundary.max.default.limit}")
    private Integer maxDefaultLimit;
}
