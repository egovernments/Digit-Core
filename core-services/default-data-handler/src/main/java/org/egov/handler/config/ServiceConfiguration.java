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

	//MDMS Configs
	@Value("${egov.mdms.host}${egov.mdms.default.data.create.endpoint}")
	private String mdmsDefaultDataCreateURI;

	@Value("#{'${default.mdms.schema.create.list}'.split(',')}")
	private List<String> defaultMdmsSchemaList;

	//Localization Configs
	@Value("${egov.localization.host}${egov.localization.default.data.create.endpoint}")
	private String localizationDefaultDataCreateURI;

	@Value("${default.localization.locale}")
	private String defaultLocalizationLocale;

	@Value("#{'${default.localization.module.create.list}'.split(',')}")
	private List<String> defaultLocalizationModuleList;

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

	// User OTP Configuration
	@Value("${egov.user.otp.host}")
	private String userOtpHost;

	@Value("${egov.user.otp.send.endpoint}")
	private String userOtpSendEndpoint;

}
