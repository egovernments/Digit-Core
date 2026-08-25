package org.egov.user.domain.service.utils;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.egov.user.config.UserServiceConstants.DEFAULT_EMAIL_UPDATION_MESSAGE;
import static org.reflections.Reflections.log;

@Component
@Slf4j

public class LocalizationUtil {

    @Value("${egov.localization.host}")
    private String localizationServiceHost;
    @Value("${egov.localization.search.endpoint}")
    private String localizationServiceSearchPath;
    @Value("${state.level.tenant.id}")
    private String tenantId;
    @Value("${egov.localization.module}")
    private String module;
    @Value("${egov.localization.default.locale}")
    private String defaultLocale;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private MultiStateInstanceUtil centralInstanceUtil;

    public String getLocalizedMessage(String code, String locale, RequestInfo requestInfo) {
        if(locale == null)
            locale = defaultLocale;
        String uri = getUri(locale);
        Object responseobj = restTemplate.postForObject(uri, requestInfo, Map.class);
        Object object = JsonPath.read(responseobj,
                "$.messages[?(@.code==\"" + code + "\")].message");
        List<String> messages = (ArrayList<String>) object;
        if(CollectionUtils.isEmpty(messages)){
            log.warn("No localization messages returned for locale: " + locale +" . Continuing with english language");
            messages.add(DEFAULT_EMAIL_UPDATION_MESSAGE);
        }
        String message = messages.get(0);
        return message;
    }

    String getUri(String locale) {
        return localizationServiceHost + localizationServiceSearchPath + "?locale=" + locale + "&tenantId=" + tenantId + "&module=" + module;
    }

    /**
     * Tenant-aware localization: derives the state-level tenant dynamically from the
     * provided tenantId instead of using the hardcoded configuration property.
     */
    public String getLocalizedMessage(String code, String locale, RequestInfo requestInfo, String userTenantId) {
        if(locale == null)
            locale = defaultLocale;
        String resolvedTenantId = (userTenantId != null)
                ? centralInstanceUtil.getStateLevelTenant(userTenantId)
                : tenantId;
        String uri = localizationServiceHost + localizationServiceSearchPath + "?locale=" + locale + "&tenantId=" + resolvedTenantId + "&module=" + module;
        Object responseobj = restTemplate.postForObject(uri, requestInfo, Map.class);
        Object object = JsonPath.read(responseobj,
                "$.messages[?(@.code==\"" + code + "\")].message");
        List<String> messages = (ArrayList<String>) object;
        if(CollectionUtils.isEmpty(messages)){
            log.warn("No localization messages returned for locale: " + locale +" . Continuing with english language");
            messages.add(DEFAULT_EMAIL_UPDATION_MESSAGE);
        }
        String message = messages.get(0);
        return message;
    }

}
