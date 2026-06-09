package org.egov.common.utils;

import java.util.regex.Pattern;

import org.egov.common.error.ErrorCode;
import org.egov.common.exception.InvalidTenantIdFormatException;
import org.egov.common.exception.InvalidTenantIdException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Configuration
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MultiStateInstanceUtil {

    // central-instance configs
	public static final String SCHEMA_REPLACE_STRING = "{schema}";

    private static final Pattern SCHEMA_NAME_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]{0,62}$");
    /*
     * Represents the length of the tenantId array when it's split by "."
     *
     * if the array length is equal or lesser than, then the tenantId belong to state level
     *
     * else it's tenant level
     */
    @Value("${state.level.tenantid.length:1}")
    private Integer stateLevelTenantIdLength;
    
    /*
     * Boolean field informing whether the deployed server is a multi-state/central-instance 
     * 
     */
    @Value("${is.environment.central.instance:false}")
    private Boolean isEnvironmentCentralInstance;
    
    /*
     * Index in which to find the schema name in a tenantId split by "."
     */
    @Value("${state.schema.index.position.tenantid:1}")
    private Integer stateSchemaIndexPositionInTenantId;

    private void validateTenantId(String tenantId) {
        if (tenantId == null || tenantId.isEmpty())
            throw new InvalidTenantIdFormatException(ErrorCode.INVALID_TENANT_ID_FORMAT);
        for (String segment : tenantId.split("\\.", -1)) {
            if (!SCHEMA_NAME_PATTERN.matcher(segment).matches())
                throw new InvalidTenantIdFormatException(ErrorCode.INVALID_TENANT_ID_FORMAT);
        }
    }

    /**
     * Resolves the schema name from a tenantId using {@code stateSchemaIndexPositionInTenantId}.
     *
     * <p>If the tenantId has no dots (single part), the tenantId itself is returned regardless
     * of the configured position. Otherwise the part at {@code min(position, parts.length-1)}
     * is returned, so the index is always in bounds.
     *
     * <pre>
     * stateSchemaIndexPositionInTenantId = 0:
     *   "pb"                    -&gt; "pb"   (single part, no dot)
     *   "pb.amritsar"           -&gt; "pb"   (min(0,1)=0)
     *   "pb.amritsar.local1"    -&gt; "pb"   (min(0,2)=0)
     *   "in.pb.amritsar.local1" -&gt; "in"   (min(0,3)=0)
     *
     * stateSchemaIndexPositionInTenantId = 1:
     *   "pb"                    -&gt; "pb"       (single part)
     *   "pb.amritsar"           -&gt; "amritsar" (min(1,1)=1)
     *   "pb.amritsar.local1"    -&gt; "amritsar" (min(1,2)=1)
     *   "pb.amritsar.local1.v"  -&gt; "amritsar" (min(1,3)=1)
     *
     * stateSchemaIndexPositionInTenantId = 2:
     *   "pb"                        -&gt; "pb"       (single part)
     *   "pb.amritsar"               -&gt; "amritsar" (min(2,1)=1, capped)
     *   "pb.amritsar.local1"        -&gt; "local1"   (min(2,2)=2)
     *   "pb.amritsar.local1.mohali" -&gt; "local1"   (min(2,3)=2, capped)
     * </pre>
     */
    private String resolveSchemaName(String tenantId) {
        String[] parts = tenantId.split("\\.");
        if (parts.length == 1) return tenantId;
        int idx = Math.min(stateSchemaIndexPositionInTenantId, parts.length - 1);
        String schema = parts[idx];
        if (!SCHEMA_NAME_PATTERN.matcher(schema).matches())
            throw new InvalidTenantIdFormatException(String.format(ErrorCode.INVALID_SCHEMA_NAME, schema));
        return schema;
    }

	/**
	 * Replaces the {@code {schema}} placeholder in a query with the schema name derived
	 * from the tenantId.
	 *
	 * <p>When central instance is enabled the schema is resolved via
	 * {@link #resolveSchemaName(String)} (see that method for the full truth table).
	 * When disabled, {@code {schema}.} is stripped so queries work against the default schema.
	 *
	 * <pre>
	 * central=true, position=1, query="select * from {schema}.employee":
	 *   "pb"                 -&gt; "select * from pb.employee"
	 *   "pb.amritsar"        -&gt; "select * from amritsar.employee"
	 *   "pb.amritsar.local1" -&gt; "select * from amritsar.employee"
	 *
	 * central=false (any tenantId):
	 *   "pb"                 -&gt; "select * from employee"
	 *   "pb.amritsar"        -&gt; "select * from employee"
	 * </pre>
	 */
	public String replaceSchemaPlaceholder(String query, String tenantId) throws InvalidTenantIdException {
		validateTenantId(tenantId);
		if (getIsEnvironmentCentralInstance()) {
			String schemaName = resolveSchemaName(tenantId);
			return query.replaceAll("(?i)" + Pattern.quote(SCHEMA_REPLACE_STRING), schemaName);
		}
		return query.replaceAll("(?i)" + Pattern.quote(SCHEMA_REPLACE_STRING.concat(".")), "");
	}

	/**
	 * Method to determine if the given tenantId belongs to tenant or state level in
	 * the current environment.
	 *
	 * <pre>
	 * central=true, stateLevelTenantIdLength=1:
	 *   "pb"                 -&gt; true  (1 part  &lt;= 1)
	 *   "pb.amritsar"        -&gt; false (2 parts &gt; 1)
	 *   "pb.amritsar.local1" -&gt; false (3 parts &gt; 1)
	 *
	 * central=true, stateLevelTenantIdLength=2:
	 *   "pb"                 -&gt; true  (1 part  &lt;= 2)
	 *   "pb.amritsar"        -&gt; true  (2 parts &lt;= 2)
	 *   "pb.amritsar.local1" -&gt; false (3 parts &gt; 2)
	 *
	 * central=false (dot check only):
	 *   "pb"                 -&gt; true  (no dot)
	 *   "pb.amritsar"        -&gt; false (has dot)
	 * </pre>
	 *
	 * @param tenantId
	 * @return
	 */
	public Boolean isTenantIdStateLevel(String tenantId) {

		if (getIsEnvironmentCentralInstance()) {

			int tenantLevel = tenantId.split("\\.").length;
			return tenantLevel <= stateLevelTenantIdLength;
		} else {
			/*
			 * if the instance is not multi-state/central-instance then tenant is always
			 * two level
			 * 
			 * if tenantId contains "." then it is tenant level
			 */
			return !tenantId.contains(".");
		}
	}
	
	/**
	 * For central instance: if the tenantId has fewer parts than {@code stateLevelTenantIdLength}
	 * the same tenantId is returned. Otherwise the tenantId is trimmed to the first
	 * {@code stateLevelTenantIdLength} dot-separated parts.
	 *
	 * <p>For non-central instance: always returns the first part of the tenantId (split by dot).
	 *
	 * <pre>
	 * central=true, stateLevelTenantIdLength=1:
	 *   "pb"                 -&gt; "pb"
	 *   "pb.amritsar"        -&gt; "pb"
	 *   "pb.amritsar.local1" -&gt; "pb"
	 *
	 * central=true, stateLevelTenantIdLength=2:
	 *   "pb"                     -&gt; "pb"
	 *   "pb.amritsar"            -&gt; "pb.amritsar"
	 *   "pb.amritsar.local1"     -&gt; "pb.amritsar"
	 *   "pb.amritsar.local1.m"   -&gt; "pb.amritsar"
	 *
	 * central=true, stateLevelTenantIdLength=3:
	 *   "pb"                         -&gt; "pb"
	 *   "pb.amritsar"                -&gt; "pb.amritsar"
	 *   "pb.amritsar.local1"         -&gt; "pb.amritsar.local1"
	 *   "pb.amritsar.local1.mohali"  -&gt; "pb.amritsar.local1"
	 *
	 * central=false (any stateLevelTenantIdLength):
	 *   "pb"                 -&gt; "pb"
	 *   "pb.amritsar"        -&gt; "pb"
	 *   "pb.amritsar.local1" -&gt; "pb"
	 * </pre>
	 *
	 * @param tenantId
	 * @return
	 */
	public String getStateLevelTenant(String tenantId) {

		String[] tenantArray = tenantId.split("\\.");
		String stateTenant = tenantArray[0];

		if (getIsEnvironmentCentralInstance()) {
			if (getStateLevelTenantIdLength() < tenantArray.length) {
				for (int i = 1; i < getStateLevelTenantIdLength(); i++)
					stateTenant = stateTenant.concat(".").concat(tenantArray[i]);
			} else {
				stateTenant = tenantId;
			}
		}
		return stateTenant;
	}

	/**
	 * Returns a state-specific Kafka topic name by prepending the schema prefix.
	 *
	 * <p>When central instance is enabled the schema is resolved via
	 * {@link #resolveSchemaName(String)} and prepended as {@code schema-topic}.
	 * When disabled, the topic is returned unchanged.
	 *
	 * <pre>
	 * central=true, position=1, topic="save-employee":
	 *   "pb"                 -&gt; "pb-save-employee"
	 *   "pb.amritsar"        -&gt; "amritsar-save-employee"
	 *   "pb.amritsar.local1" -&gt; "amritsar-save-employee"
	 *
	 * central=true, position=0, topic="save-employee":
	 *   "pb"             -&gt; "pb-save-employee"
	 *   "pb.amritsar"    -&gt; "pb-save-employee"
	 *   "in.pb.amritsar" -&gt; "in-save-employee"
	 *
	 * central=false (any tenantId):
	 *   "pb"             -&gt; "save-employee"
	 *   "pb.amritsar"    -&gt; "save-employee"
	 * </pre>
	 *
	 * @param tenantId
	 * @param topic
	 * @return
	 */
	public String getStateSpecificTopicName(String tenantId, String topic) {
		validateTenantId(tenantId);
		if (getIsEnvironmentCentralInstance()) {
			String schema = resolveSchemaName(tenantId);
			return schema.concat("-").concat(topic);
		}
		return topic;
	}
}
