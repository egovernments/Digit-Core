package org.egov.enc;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.egov.enc.masterdata.MasterDataProvider;
import org.egov.enc.utils.Constants;
import org.egov.tracer.model.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.util.ObjectUtils;

@SpringBootApplication
@ComponentScan(basePackages = { "org.egov.enc", "org.egov.enc.web.controllers" , "org.egov.enc.config"})
public class Main {

    @Value("${egov.mdms.provider}")
    private String masterDataProviderClassName;

    public static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public MasterDataProvider masterDataProvider() {
        MasterDataProvider masterDataProvider = null;
        try{
            if(ObjectUtils.isEmpty(masterDataProviderClassName)){
                masterDataProviderClassName = Constants.DEFAULT_MASTER_DATA_PROVIDER;
            }
            Class<?> masterDataProviderClass = Class.forName(masterDataProviderClassName);

            masterDataProvider = (MasterDataProvider) masterDataProviderClass.newInstance();
            LOGGER.info("Invoked MasterDataProvider with Classname: {}", masterDataProviderClassName);
        }catch(ClassNotFoundException | InstantiationException | IllegalAccessException e){
            LOGGER.error("MDMS provider class {} cannot be instantiate with exception: {}", masterDataProviderClassName, ExceptionUtils.getStackTrace(e));
            throw new CustomException("Unable to load MDMS provider class", "MDMS Provider Init Exception");
        }
        return masterDataProvider;
    }

}
