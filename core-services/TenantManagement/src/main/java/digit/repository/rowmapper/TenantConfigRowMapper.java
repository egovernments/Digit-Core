package digit.repository.rowmapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import digit.web.models.Document;
import digit.web.models.Tenant;
import digit.web.models.TenantConfig;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.models.AuditDetails;
import org.egov.tracer.model.CustomException;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static digit.errors.ErrorCodes.INVALID_JSON;
import static digit.errors.ErrorCodes.INVALID_JSON_MSG;
import static java.util.Objects.isNull;

@Slf4j
@Component
public class TenantConfigRowMapper implements RowMapper<TenantConfig> {

    @Override
    public TenantConfig mapRow(ResultSet rs, int rowNum) throws SQLException {

        // Create the tenantConfig
        TenantConfig tenantConfig = new TenantConfig();

        try {
            tenantConfig.setId(rs.getString("tenantConfigId"));
            tenantConfig.setCode(rs.getString("code"));
            tenantConfig.setDefaultLoginType(rs.getString("defaultLoginType"));
            tenantConfig.setEnableUserBasedLogin(rs.getBoolean("enableUserBasedLogin"));
            tenantConfig.setAdditionalAttributes(rs.getString("additionalAttributes"));
            tenantConfig.setIsActive(rs.getBoolean("isActive"));
            tenantConfig.setName(rs.getString("name"));
            tenantConfig.setOtpLength(rs.getString("otpLength"));
            String languagesJson = rs.getString("languages");
            if (languagesJson != null && !languagesJson.isEmpty()) {
                tenantConfig.setLanguages(new ObjectMapper().readValue(languagesJson, new TypeReference<List<String>>() {
                }));
            } else {
                tenantConfig.setLanguages(Collections.emptyList());
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        AuditDetails auditDetails = new AuditDetails().builder()
                .createdBy(rs.getString("createdBy"))
                .lastModifiedBy(rs.getString("lastModifiedBy"))
                .createdTime(rs.getLong("createdTime"))
                .lastModifiedBy(rs.getString("lastModifiedTime"))
                .build();

        tenantConfig.setAuditDetails(auditDetails);

        // Create an array to hold the documents
        List<Document> documents = new ArrayList<>();

        // Populate the documents array
        do {
            Document document = new Document();
            document.setId(rs.getString("documentId"));  // Assuming alias as document_id
            document.setTenantConfigId(rs.getString("tenantConfigId"));
            document.setTenantId(rs.getString("tenantId"));
            document.setType(rs.getString("type"));
            document.setFileStoreId(rs.getString("fileStoreId"));
            document.setUrl(rs.getString("url"));
            document.setIsActive(rs.getBoolean("documentIsActive"));

            AuditDetails docAuditDetails = new AuditDetails().builder()
                    .createdBy(rs.getString("documentCreatedBy"))
                    .lastModifiedBy(rs.getString("documentLastModifiedBy"))
                    .createdTime(rs.getLong("documentCreatedTime"))
                    .lastModifiedBy(rs.getString("documentLastModifiedTime"))
                    .build();

            document.setAuditDetails(docAuditDetails);

            documents.add(document);
        } while (rs.next() && rs.getString("tenantConfigId").equals(tenantConfig.getId()));

        // Set the documents in tenantConfig
        tenantConfig.setDocuments(documents);

        return tenantConfig;
    }
}

