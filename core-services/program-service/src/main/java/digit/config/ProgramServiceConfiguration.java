package digit.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.*;
import org.egov.tracer.config.TracerConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;

import java.util.TimeZone;

@Component
@Data
@Import({TracerConfiguration.class})
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ProgramServiceConfiguration {

	@Value("${app.timezone}")
	private String timeZone;

	@PostConstruct
	public void initialize() {
		TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
	}

	@Bean
	@Autowired
	public MappingJackson2HttpMessageConverter jacksonConverter(ObjectMapper objectMapper) {
		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		converter.setObjectMapper(objectMapper);
		return converter;
	}

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

	@Value("${is.workflow.enabled}")
	private Boolean isWorkflowEnabled;

	// Program Variables
	@Value("${pg.kafka.create.topic}")
	private String programCreateTopic;

	@Value("${pg.kafka.update.topic}")
	private String programUpdateTopic;

	@Value("${pg.default.offset}")
	private Integer programDefaultOffset;

	@Value("${pg.default.limit}")
	private Integer programDefaultLimit;

	@Value("${pg.search.max.limit}")
	private Integer programMaxLimit;

	// Agency Variables
	@Value("${ag.kafka.create.topic}")
	private String agencyCreateTopic;

	@Value("${ag.kafka.update.topic}")
	private String agencyUpdateTopic;

	@Value("${ag.default.offset}")
	private Integer agencyDefaultOffset;

	@Value("${ag.default.limit}")
	private Integer agencyDefaultLimit;

	@Value("${ag.search.max.limit}")
	private Integer agencyMaxLimit;

	// Project Variables
	@Value("${pj.kafka.create.topic}")
	private String projectCreateTopic;

	@Value("${pj.kafka.update.topic}")
	private String projectUpdateTopic;

	@Value("${pj.default.offset}")
	private Integer projectDefaultOffset;

	@Value("${pj.default.limit}")
	private Integer projectDefaultLimit;

	@Value("${pj.search.max.limit}")
	private Integer projectMaxLimit;

	// Estimate Variables
	@Value("${es.kafka.create.topic}")
	private String estimateCreateTopic;

	@Value("${es.kafka.update.topic}")
	private String estimateUpdateTopic;

	@Value("${es.default.offset}")
	private Integer estimateDefaultOffset;

	@Value("${es.default.limit}")
	private Integer estimateDefaultLimit;

	@Value("${es.search.max.limit}")
	private Integer estimateMaxLimit;

	// Sanction Variables
	@Value("${sa.kafka.create.topic}")
	private String sanctionCreateTopic;

	@Value("${sa.kafka.update.topic}")
	private String sanctionUpdateTopic;

	@Value("${sa.default.offset}")
	private Integer sanctionDefaultOffset;

	@Value("${sa.default.limit}")
	private Integer sanctionDefaultLimit;

	@Value("${sa.search.max.limit}")
	private Integer sanctionMaxLimit;

	// Allocation Variables
	@Value("${al.kafka.create.topic}")
	private String allocationCreateTopic;

	@Value("${al.kafka.update.topic}")
	private String allocationUpdateTopic;

	@Value("${al.default.offset}")
	private Integer allocationDefaultOffset;

	@Value("${al.default.limit}")
	private Integer allocationDefaultLimit;

	@Value("${al.search.max.limit}")
	private Integer allocationMaxLimit;

	// Disburse Variables
	@Value("${di.kafka.create.topic}")
	private String disburseCreateTopic;

	@Value("${di.kafka.update.topic}")
	private String disburseUpdateTopic;

	@Value("${di.default.offset}")
	private Integer disburseDefaultOffset;

	@Value("${di.default.limit}")
	private Integer disburseDefaultLimit;

	@Value("${di.search.max.limit}")
	private Integer disburseMaxLimit;

	//MDMS
	@Value("${egov.mdms.host}")
	private String mdmsHost;

	@Value("${egov.mdms.search.endpoint}")
	private String mdmsEndPoint;

	//HRMS
	//@Value("${egov.hrms.host}")
	//private String hrmsHost;

	//@Value("${egov.hrms.search.endpoint}")
	//private String hrmsEndPoint;

	@Value("${egov.url.shortner.host}")
	private String urlShortnerHost;

	@Value("${egov.url.shortner.endpoint}")
	private String urlShortnerEndpoint;

	@Value("${egov.sms.notification.topic}")
	private String smsNotificationTopic;

}
