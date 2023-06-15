package org.egov.infra.mdms.repository;

import net.minidev.json.JSONArray;
import org.egov.infra.mdms.model.*;
import org.egov.mdms.model.MdmsCriteria;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public interface MdmsDataRepository {
    public void create(MdmsRequest mdmsRequest);

    public void update(MdmsRequest mdmsRequest);

    public  Map<String, JSONArray> search(Set<String> schemaCodes);

}
