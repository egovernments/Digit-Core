package org.egov.infra.mdms.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.models.AuditDetails;
import org.egov.infra.mdms.model.AuditLog;
import org.egov.infra.mdms.model.Mdms;
import org.egov.infra.mdms.model.MdmsHistory;
import org.egov.infra.mdms.model.MdmsVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.egov.infra.mdms.utils.MDMSConstants.*;

@Component
@Slf4j
public class HistoryUtil {

    private static final Set<String> CREATE_OPERATIONS = new HashSet<>(Arrays.asList("CREATE", "UPSERT"));

    private ObjectMapper objectMapper;

    @Autowired
    public HistoryUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * This method builds the history of a master data record from its audit logs.
     * @param mdms the live master data record
     * @param auditLogs audit logs of the record, at most one more than maxVersions
     * @param maxVersions maximum number of versions to return
     * @return
     */
    public MdmsHistory buildHistory(Mdms mdms, List<AuditLog> auditLogs, int maxVersions) {
        boolean truncated = auditLogs.size() > maxVersions;

        // Latest first; lastModifiedTime is set from server time by mdms-v2, changeDate and id break ties
        auditLogs.sort(Comparator.comparing(this::getModifiedTime, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(AuditLog::getChangeDate, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(AuditLog::getId, Comparator.nullsLast(Comparator.reverseOrder())));

        List<MdmsVersion> versions = new ArrayList<>();
        for (AuditLog auditLog : truncated ? auditLogs.subList(0, maxVersions) : auditLogs)
            versions.add(getVersion(mdms, auditLog));

        boolean baselineMissing = !truncated && auditLogs.stream()
                .noneMatch(auditLog -> CREATE_OPERATIONS.contains(auditLog.getOperationType()));

        // The live record is added only when it holds a value that no audit log carries
        if (baselineMissing && isLiveRecordNewer(mdms, versions)) {
            versions.add(0, getCurrentSnapshot(mdms));

            // Keep the version cap after adding the live record
            if (versions.size() > maxVersions) {
                versions.remove(versions.size() - 1);
                truncated = true;
            }
        }

        return MdmsHistory.builder()
                .id(mdms.getId())
                .tenantId(mdms.getTenantId())
                .schemaCode(mdms.getSchemaCode())
                .uniqueIdentifier(mdms.getUniqueIdentifier())
                .isActive(mdms.getIsActive())
                .baselineMissing(baselineMissing)
                .truncated(truncated)
                .versions(versions)
                .build();
    }

    private MdmsVersion getVersion(Mdms mdms, AuditLog auditLog) {
        Map<String, Object> keyValueMap = getKeyValueMap(auditLog);

        return MdmsVersion.builder()
                .operation(auditLog.getOperationType())
                .data(getJsonNode(keyValueMap.get(AUDIT_DATA_KEY)))
                .isActive(getBoolean(keyValueMap.get(AUDIT_IS_ACTIVE_KEY)))
                .auditDetails(getAuditDetails(mdms, auditLog))
                .auditLogId(auditLog.getId())
                .integrityHash(auditLog.getIntegrityHash())
                .build();
    }

    // Update audit logs carry only the columns an update writes, so createdBy/createdTime come from the
    // record; an update never changes them
    private AuditDetails getAuditDetails(Mdms mdms, AuditLog auditLog) {
        Map<String, Object> keyValueMap = getKeyValueMap(auditLog);
        AuditDetails recordAuditDetails = Objects.isNull(mdms.getAuditDetails()) ? AuditDetails.builder().build() : mdms.getAuditDetails();
        Object createdBy = keyValueMap.get(AUDIT_CREATED_BY_KEY);
        Long createdTime = getLong(keyValueMap.get(AUDIT_CREATED_TIME_KEY));
        Object lastModifiedBy = keyValueMap.get(AUDIT_LAST_MODIFIED_BY_KEY);

        return AuditDetails.builder()
                .createdBy(Objects.isNull(createdBy) ? recordAuditDetails.getCreatedBy() : createdBy.toString())
                .createdTime(Objects.isNull(createdTime) ? recordAuditDetails.getCreatedTime() : createdTime)
                .lastModifiedBy(Objects.isNull(lastModifiedBy) ? auditLog.getUserUUID() : lastModifiedBy.toString())
                .lastModifiedTime(getModifiedTime(auditLog))
                .build();
    }

    private MdmsVersion getCurrentSnapshot(Mdms mdms) {
        return MdmsVersion.builder()
                .operation(CURRENT_SNAPSHOT)
                .data(mdms.getData())
                .isActive(mdms.getIsActive())
                .auditDetails(mdms.getAuditDetails())
                .build();
    }

    private boolean isLiveRecordNewer(Mdms mdms, List<MdmsVersion> versions) {
        if (versions.isEmpty())
            return true;
        Long liveModifiedTime = Objects.isNull(mdms.getAuditDetails()) ? null : mdms.getAuditDetails().getLastModifiedTime();
        Long latestAuditedTime = versions.get(0).getAuditDetails().getLastModifiedTime();
        return !Objects.isNull(liveModifiedTime) && (Objects.isNull(latestAuditedTime) || liveModifiedTime > latestAuditedTime);
    }

    private Map<String, Object> getKeyValueMap(AuditLog auditLog) {
        return Objects.isNull(auditLog.getKeyValueMap()) ? Collections.emptyMap() : auditLog.getKeyValueMap();
    }

    private Long getModifiedTime(AuditLog auditLog) {
        Long modifiedTime = getLong(getKeyValueMap(auditLog).get(AUDIT_LAST_MODIFIED_TIME_KEY));
        return Objects.isNull(modifiedTime) ? auditLog.getChangeDate() : modifiedTime;
    }

    private Long getLong(Object value) {
        if (value instanceof Number)
            return ((Number) value).longValue();
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                log.error("Invalid numeric value in audit log: {}", value);
            }
        }
        return null;
    }

    // audit-service stores a JSONB value as a serialised PGobject: {"type":"jsonb","value":"<json>"}
    private JsonNode getJsonNode(Object value) {
        if (Objects.isNull(value))
            return null;
        JsonNode node = objectMapper.valueToTree(value);
        if (node.isObject() && node.has("type") && node.path("value").isTextual())
            return readTree(node.get("value").asText(), node);
        if (node.isTextual())
            return readTree(node.asText(), node);
        return node;
    }

    private JsonNode readTree(String json, JsonNode defaultNode) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            return defaultNode;
        }
    }

    private Boolean getBoolean(Object value) {
        if (value instanceof Boolean)
            return (Boolean) value;
        if (value instanceof String)
            return Boolean.parseBoolean((String) value);
        return null;
    }
}
