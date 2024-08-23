package org.egov.handler.config;

import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ServiceConfiguration {

	@Value("${kafka.topics.create.tenant}")
	private String createTopic;


	@Value("${egov.mdms.host}${egov.mdms.default.data.create.endpoint}")
	private String mdmsDefaultDataCreateURI;

	@Value("#{'${default.mdms.create.list}'.split(',')}")
	private List<String> defaultMdmsSchemaList;

}
