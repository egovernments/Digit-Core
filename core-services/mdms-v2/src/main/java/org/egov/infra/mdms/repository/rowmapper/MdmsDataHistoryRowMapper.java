package org.egov.infra.mdms.repository.rowmapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.models.AuditDetails;
import org.egov.infra.mdms.model.ChangeDetails;
import org.egov.infra.mdms.model.HistoryOperation;
import org.egov.infra.mdms.model.Mdms;
import org.egov.infra.mdms.model.MdmsHistory;
import org.egov.tracer.model.CustomException;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;
import static org.egov.infra.mdms.errors.ErrorCodes.INVALID_JSON;
import static org.egov.infra.mdms.errors.ErrorCodes.INVALID_JSON_MSG;

@Component
public class MdmsDataHistoryRowMapper implements ResultSetExtractor<List<MdmsHistory>> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<MdmsHistory> extractData(ResultSet resultSet) throws SQLException, DataAccessException {

        List<MdmsHistory> mdmsHistoryList = new ArrayList<>();

        while (resultSet.next()) {
            AuditDetails auditDetails = AuditDetails.builder().createdBy(resultSet.getString("createdby")).
                    createdTime(resultSet.getLong("createdtime")).
                    lastModifiedBy(resultSet.getString("lastmodifiedby")).
                    lastModifiedTime(resultSet.getLong("lastmodifiedtime")).build();

            JsonNode data = null;
            if (!isNull(resultSet.getObject("data"))) {
                String dataStr = ((PGobject) resultSet.getObject("data")).getValue();
                try {
                    data = objectMapper.readTree(dataStr);
                } catch (IOException e) {
                    throw new CustomException(INVALID_JSON, INVALID_JSON_MSG);
                }
            }

            Mdms mdms = Mdms.builder()
                    .id(resultSet.getString("id"))
                    .tenantId(resultSet.getString("tenantid"))
                    .schemaCode(resultSet.getString("schemacode"))
                    .uniqueIdentifier(resultSet.getString("uniqueidentifier"))
                    .data(data)
                    .isActive(resultSet.getBoolean("isactive"))
                    .auditDetails(auditDetails).build();

            ChangeDetails changeDetails = ChangeDetails.builder()
                    .historyId(resultSet.getLong("historyid"))
                    .version(resultSet.getInt("version"))
                    .operation(HistoryOperation.fromValue(resultSet.getString("operation")))
                    .changedTime(resultSet.getLong("changedtime"))
                    .dbUser(resultSet.getString("dbuser"))
                    .clientAddress(resultSet.getString("clientaddress"))
                    .applicationName(resultSet.getString("applicationname"))
                    .transactionId(resultSet.getLong("transactionid")).build();

            mdmsHistoryList.add(MdmsHistory.builder().changeDetails(changeDetails).mdms(mdms).build());
        }

        return mdmsHistoryList;
    }
}
