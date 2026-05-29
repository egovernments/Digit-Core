package org.egov.infra.mdms.config;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@ToString
@Setter
@Getter
@Import({MultiStateInstanceUtil.class})
public class ApplicationConfig {

    @Value("${egov.mdms.schema.definition.save.topic}")
    private String saveSchemaDefinitionTopicName;

    @Value("${egov.mdms.data.save.topic}")
    private String saveMdmsDataTopicName;

    @Value("${egov.mdms.data.update.topic}")
    private String updateMdmsDataTopicName;

    @Value("${mdms.default.offset}")
    private Integer defaultOffset;

    @Value("${mdms.default.limit}")
    private Integer defaultLimit;

    @Value("${mdms.search.result.limit:1000}")
    private Integer searchResultLimit;

    @Value("${mdms.no.limit.schema.codes:}")
    private String noLimitSchemaCodesRaw;

    @Setter(AccessLevel.NONE) // computed from noLimitSchemaCodesRaw at startup — must not be overwritten
    private Set<String> noLimitSchemaCodes;

    @PostConstruct
    public void init() {
        if (!StringUtils.hasText(noLimitSchemaCodesRaw)) {
            noLimitSchemaCodes = Collections.emptySet();
        } else {
            noLimitSchemaCodes = Arrays.stream(noLimitSchemaCodesRaw.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toUnmodifiableSet());
        }
    }

    public Set<String> getNoLimitSchemaCodes() {
        return noLimitSchemaCodes;
    }

}
