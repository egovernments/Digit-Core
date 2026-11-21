package org.egov.enc.masterdata;

import org.egov.tracer.model.CustomException;

import java.util.ArrayList;

public interface MasterDataProvider {
    ArrayList<String> getTenantIds() throws CustomException;
}
