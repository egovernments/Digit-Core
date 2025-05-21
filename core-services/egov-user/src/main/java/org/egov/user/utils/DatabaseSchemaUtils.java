package org.egov.user.utils;

import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import org.egov.common.exception.InvalidTenantIdException;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import static org.egov.user.config.UserServiceConstants.INVALID_TENANT_ID_ERR_CODE;

@Configuration
//@Component
//@AllArgsConstructor
public class DatabaseSchemaUtils {
    public static String SCHEMA_REPLACE_STRING = "{schema}";
    @Value("${state.level.tenantid.length:1}")
    private Integer stateLevelTenantIdLength;
    @Value("${is.environment.central.instance:false}")
    private Boolean isEnvironmentCentralInstance;
    @Value("${state.schema.index.position.tenantid:1}")
    private Integer stateSchemaIndexPositionInTenantId;

    public String replaceSchemaPlaceholder(String query, String tenantId) {
        String finalQuery = null;
        if (this.getIsEnvironmentCentralInstance()) {
            if (this.stateSchemaIndexPositionInTenantId >= tenantId.length()) {
                throw new CustomException(INVALID_TENANT_ID_ERR_CODE, "The tenantId length is smaller than the defined schema index in tenantId for central instance");
            }

            String schemaName;
            if (tenantId.contains(".")) {
                schemaName = tenantId.split("\\.")[this.getStateSchemaIndexPositionInTenantId()];
            } else {
                schemaName = tenantId;
            }

            finalQuery = query.replaceAll("(?i)" + Pattern.quote(SCHEMA_REPLACE_STRING), schemaName);
        } else {
            finalQuery = query.replaceAll("(?i)" + Pattern.quote(SCHEMA_REPLACE_STRING.concat(".")), "");
        }

        return finalQuery;
    }

    public Boolean isTenantIdStateLevel(String tenantId) {
        if (this.getIsEnvironmentCentralInstance()) {
            int tenantLevel = tenantId.split("\\.").length;
            return tenantLevel <= this.stateLevelTenantIdLength;
        } else {
            return !tenantId.contains(".");
        }
    }

    public String getStateLevelTenant(String tenantId) {
        String[] tenantArray = tenantId.split("\\.");
        String stateTenant = tenantArray[0];
        if (this.getIsEnvironmentCentralInstance()) {
            if (this.getStateLevelTenantIdLength() < tenantArray.length) {
                for(int i = 1; i < this.getStateLevelTenantIdLength(); ++i) {
                    stateTenant = stateTenant.concat(".").concat(tenantArray[i]);
                }
            } else {
                stateTenant = tenantId;
            }
        }

        return stateTenant;
    }

    public String getStateSpecificTopicName(String tenantId, String topic) {
        String updatedTopic = topic;
        if (this.getIsEnvironmentCentralInstance()) {
            String[] tenants = tenantId.split("\\.");
            if (tenantId.contains(".") && tenants.length > 1) {
                updatedTopic = tenants[this.stateSchemaIndexPositionInTenantId].concat("-").concat(topic);
            } else {
                updatedTopic = tenantId.concat("-").concat(topic);
            }
        }

        return updatedTopic;
    }

    public Integer getStateLevelTenantIdLength() {
        return this.stateLevelTenantIdLength;
    }

    public Boolean getIsEnvironmentCentralInstance() {
        return this.isEnvironmentCentralInstance;
    }

    public Integer getStateSchemaIndexPositionInTenantId() {
        return this.stateSchemaIndexPositionInTenantId;
    }

    public DatabaseSchemaUtils(final Integer stateLevelTenantIdLength, final Boolean isEnvironmentCentralInstance, final Integer stateSchemaIndexPositionInTenantId) {
        this.stateLevelTenantIdLength = stateLevelTenantIdLength;
        this.isEnvironmentCentralInstance = isEnvironmentCentralInstance;
        this.stateSchemaIndexPositionInTenantId = stateSchemaIndexPositionInTenantId;
    }

    public DatabaseSchemaUtils() {
    }

}
