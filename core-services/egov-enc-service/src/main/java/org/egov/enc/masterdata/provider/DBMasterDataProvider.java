package org.egov.enc.masterdata.provider;

import org.egov.enc.masterdata.MasterDataProvider;
import org.egov.enc.models.MDMSConfig;
import org.egov.enc.repository.MDMSConfigRepository;
import org.egov.enc.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DBMasterDataProvider implements MasterDataProvider {

    @Autowired
    private MDMSConfigRepository mdmsConfigRepository;

    public static final Logger LOGGER = LoggerFactory.getLogger(DBMasterDataProvider.class);

    @Override
    public ArrayList<String> getTenantIds() {
        LOGGER.info("Inside DBMasterDataProvider");
        List<MDMSConfig> mdmsConfigList = mdmsConfigRepository.fetchMDMSConfig();
        ArrayList<String> tenantIdList = null;
        if(mdmsConfigList == null || mdmsConfigList.isEmpty()){
            tenantIdList = new ArrayList<>(Arrays.asList(Constants.DEFAULT_TENANT_ID));
        }else{
            tenantIdList = mdmsConfigList.stream().map(MDMSConfig::getTenantId)
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        return tenantIdList;
    }
}
