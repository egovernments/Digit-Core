package org.egov.sunbirdrc.repository;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.egov.sunbirdrc.models.DidSchemaId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Getter
@Setter
public class DidSchemaIdRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Map<String, DidSchemaId> dataMap = new HashMap<>();

    @PostConstruct
    public void loadData() {
        String sql = "SELECT uuid, did, schemaid FROM uuid_did_schemaid_mapper";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        for (Map<String, Object> row : rows) {
            String uuid = (String) row.get("uuid");
            String did = (String) row.get("did");
            String schemaId = (String) row.get("schemaid");
            DidSchemaId rowData = new DidSchemaId(did, schemaId);
            dataMap.put(uuid, rowData);
        }
        System.out.println("loaded data is" + dataMap);
    }

    public DidSchemaId getRowData(String uuid) {
        return dataMap.get(uuid);
    }
}
