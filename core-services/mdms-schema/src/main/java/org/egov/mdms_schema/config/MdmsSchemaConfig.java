package org.egov.mdms_schema.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@ToString
@Setter
@Getter
public class MdmsSchemaConfig {

	@Value("${egov.mdms-v2.host}")
	private String mdmsV2Host;

	@Value("${egov.mdms-v2.localhost}")
	private String mdmsV2LocalHost;

	@Value("${egov.mdms-v2.schema.create.endpoint}")
	private String mdmsV2SchemaCreate;

	@Value("${egov.mdms-v2.schema.search.endpoint}")
	private String mdmsV2SchemaSearch;

	@Value("${egov.mdms-v2.data.create.endpoint}")
	private String mdmsV2DataCreate;

	@Value("${egov.mdms-v2.data.search.endpoint}")
	private String mdmsV2DataSearch;

	@Value("${egov.mdms-schema.default.tenant.id}")
	private String defaultTenantId;

	@Value("${egov.mdms-schema.schema-code.list}")
	private String schemaCodeList;
}
