package org.egov.user.utils;

import java.util.regex.Pattern;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import static org.egov.user.config.UserServiceConstants.INVALID_TENANT_ID_ERR_CODE;

/**
 * Utility class for handling database schema operations, specifically in multi-tenancy environments.
 * Provides methods for managing tenant-specific schema replacement, state-level tenant identification,
 * and environment-specific topic modifications.
 *
 * This class is designed to work in environments that differentiate between central instance
 * configurations and non-central instance configurations.
 */
@Configuration
public class DatabaseSchemaUtils {
    public static String SCHEMA_REPLACE_STRING = "{schema}";
    @Value("${state.level.tenantid.length:1}")
    private Integer stateLevelTenantIdLength;
    @Value("${is.environment.central.instance:false}")
    private Boolean isEnvironmentCentralInstance;
    @Value("${state.schema.index.position.tenantid:1}")
    private Integer stateSchemaIndexPositionInTenantId;

    /**
     * Replaces the schema placeholder in the provided query string with the correct schema name
     * based on the tenant identifier and the environment configuration.
     *
     * @param query the SQL query string containing the schema placeholder to be replaced
     * @param tenantId the tenant identifier used to determine the schema name
     * @return the query string with the schema placeholder replaced by the appropriate schema name
     * @throws CustomException if the tenantId length is smaller than the defined schema index in a central instance
     */
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

            // replaces schema placeholder with the schema name
            finalQuery = query.replaceAll("(?i)" + Pattern.quote(SCHEMA_REPLACE_STRING), schemaName);
        } else {
            // removes the schema placeholder along with a dot after it
            finalQuery = query.replaceAll("(?i)" + Pattern.quote(SCHEMA_REPLACE_STRING.concat(".")), "");
        }

        return finalQuery;
    }

    /**
     * Determines whether the given tenant identifier is at the state level.
     * A tenant identifier is considered state-level if:
     * - In a central instance environment, the tenant ID's split level is less than
     *   or equal to the state-level tenant ID length.
     * - In a non-central instance environment, the tenant ID does not contain a period.
     *
     * @param tenantId the tenant identifier to check for state level
     * @return true if the tenant identifier is considered at the state level, false otherwise
     */
    public Boolean isTenantIdStateLevel(String tenantId) {
        if (this.getIsEnvironmentCentralInstance()) {
            int tenantLevel = tenantId.split("\\.").length;
            return tenantLevel <= this.stateLevelTenantIdLength;
        } else {
            return !tenantId.contains(".");
        }
    }

    /**
     * Retrieves the state-level tenant identifier from the given tenant ID.
     * If the environment is a central instance, the method ensures that the returned
     * identifier adheres to the state-level length rules; otherwise, the full tenant ID
     * is returned. In non-central instance environments, the first section of the
     * tenant ID (split by a period) is returned as the state-level tenant.
     *
     * @param tenantId the full tenant identifier based on which the state-level tenant
     *                 is to be derived
     * @return the state-level tenant identifier, which may differ depending on the
     *         central instance environment configuration and the tenant ID structure
     */
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

    /**
     * Constructs a state-specific topic name based on the provided tenant identifier and topic name.
     * If the environment is configured as a central instance, the method modifies the topic name
     * to include a state-specific prefix derived from the tenant identifier. Otherwise, the original
     * topic name may be concatenated with the tenant identifier.
     *
     * @param tenantId the tenant identifier used to derive the state-specific prefix
     * @param topic the base topic name to be modified or kept unchanged
     * @return the state-specific topic name constructed based on the tenant identifier and environment settings
     */
    public String getStateSpecificTopicName(String tenantId, String topic) {
        String updatedTopic = topic;
        if (this.getIsEnvironmentCentralInstance()) {
            String[] tenants = tenantId.split("\\.");
            if (tenantId.contains(".") && tenants.length > 1 && this.stateSchemaIndexPositionInTenantId < tenants.length) {
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
