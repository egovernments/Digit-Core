import { AddFilled, Button, Header, InboxSearchComposer, Loader, Dropdown } from "@egovernments/digit-ui-react-components";
import React, { useState, useEffect, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { useHistory, useParams } from "react-router-dom";
import { Config as Configg } from "../../configs/searchMDMSConfig";
import _, { drop } from "lodash";



function sortByKey(arr, key) {
  return arr.slice().sort((a, b) => {
    const valueA = a[key];
    const valueB = b[key];

    if (valueA < valueB) {
      return -1;
    } else if (valueA > valueB) {
      return 1;
    } else {
      return 0;
    }
  });
}


const MDMSManageMaster = () => {
  let Config = _.clone(Configg)
  const { t } = useTranslation();
  const history = useHistory();
  
  let {masterName:modulee,moduleName:master,tenantId} = Digit.Hooks.useQueryParams()
  
  const [availableSchemas, setAvailableSchemas] = useState([]);
  const [currentSchema, setCurrentSchema] = useState(null);
  const [masterName, setMasterName] = useState(null); //for dropdown
  const [moduleName, setModuleName] = useState(null); //for dropdown
  const [masterOptions,setMasterOptions] = useState([])
  const [moduleOptions,setModuleOptions] = useState([])
  const [updatedConfig,setUpdatedConfig] = useState(null)
  tenantId = tenantId || Digit.ULBService.getCurrentTenantId();
  const SchemaDefCriteria = {
    tenantId:tenantId ,
    limit:50
  }
  if(master && modulee ) {
    SchemaDefCriteria.codes = [`${master}.${modulee}`] 
  }

  const toDropdownObj = (master = "", mod = "") => {
    return {
      name: mod || master,
      code: Digit.Utils.locale.getTransformedLocale(mod ? `WBH_MDMS_${master}_${mod}` : `WBH_MDMS_MASTER_${master}`),
      translatedValue:t(Digit.Utils.locale.getTransformedLocale(mod ? `WBH_MDMS_${master}_${mod}` : `WBH_MDMS_MASTER_${master}`))
    };
  };

  const { isLoading, data: dropdownData } = Digit.Hooks.useCustomAPIHook({
    url: "/mdms-v2/schema/v1/_search",
    params: {
      
    },
    body: {
      SchemaDefCriteria
    },
    config: {
      select: (data) => {
        function onlyUnique(value, index, array) {
          return array.indexOf(value) === index;
        }
        
        //when api is working fine change here(thsese are all schemas available in a tenant)
        // const schemas = sampleSchemaResponse.SchemaDefinitions;
        const schemas = data?.SchemaDefinitions
        setAvailableSchemas(schemas);
        if(schemas?.length===1) setCurrentSchema(schemas?.[0])
        //now extract moduleNames and master names from this schema
        const obj = {
          mastersAvailable: [],
        };
        schemas.forEach((schema, idx) => {
          const { code } = schema;
          const splittedString = code.split(".");
          const [master, mod] = splittedString;
          obj[master] = obj[master]?.length > 0 ? [...obj[master], toDropdownObj(master, mod)] : [toDropdownObj(master, mod)];
          obj.mastersAvailable.push(master);
        });
        
        obj.mastersAvailable = obj.mastersAvailable.filter(onlyUnique)
        obj.mastersAvailable = obj.mastersAvailable.map((mas) => toDropdownObj(mas));
        //sorting based on localised value
        obj.mastersAvailable = sortByKey(obj.mastersAvailable,'translatedValue')
        return obj;
      },
    },
  });


  useEffect(() => {
    setMasterOptions(dropdownData?.mastersAvailable)
  }, [dropdownData])

  useEffect(() => {
    if(dropdownData?.[masterName?.name]?.length>0){
    setModuleOptions(sortByKey(dropdownData?.[masterName?.name],'translatedValue'))
    }
  }, [masterName])

  useEffect(() => {
    //here set current schema based on module and master name
    if(masterName?.name && moduleName?.name){
    setCurrentSchema(availableSchemas.filter(schema => schema.code === `${masterName?.name}.${moduleName?.name}`)?.[0])
    history.push(`/${window?.contextPath}/employee/workbench/mdms-search-v2?moduleName=${masterName.name}&masterName=${moduleName.name}`)
    }
  }, [moduleName])
  
  // useEffect(() => {
  //   if (currentSchema) {
  //     const dropDownOptions = [];
  //     const {
  //       definition: { properties },
  //     } = currentSchema;
      
  //     Object.keys(properties)?.forEach((key) => {
  //       if (properties[key].type === "string" && !properties[key].format) {
  //         dropDownOptions.push({
  //           // name: key,
  //           name:key,
  //           code: key,
  //           i18nKey:Digit.Utils.locale.getTransformedLocale(`${currentSchema.code}_${key}`)
  //         });
  //       }
  //     });

  //     Config.sections.search.uiConfig.fields[0].populators.options = dropDownOptions;
  //     Config.actionLink=Config.actionLink+`?moduleName=${masterName?.name}&masterName=${moduleName?.name}`;
  //     // Config.apiDetails.serviceName = `/mdms-v2/v2/_search/${currentSchema.code}`
      
      
  //     Config.additionalDetails = {
  //       currentSchemaCode:currentSchema.code
  //     }
  //     //set the column config
      
  //     Config.sections.searchResult.uiConfig.columns = [{
  //       label: "WBH_UNIQUE_IDENTIFIER",
  //       jsonPath: "uniqueIdentifier",
  //       additionalCustomization:true
  //     },...dropDownOptions.map(option => {
  //       return {
  //         label:option.i18nKey,
  //         i18nKey:option.i18nKey,
  //         jsonPath:`data.${option.code}`,
  //         dontShowNA:true
  //       }
  //     })]

  //     setUpdatedConfig(Config)
  //   }
  // }, [currentSchema]);

  if (isLoading) return <Loader />;
  return (
    <React.Fragment>
        <Header className="works-header-search">{t(Config?.label)}</Header>
      <div className="jk-header-btn-wrapper">
        <Dropdown
          option={masterOptions}
          style={{width:"25%",marginRight:"1rem" }}
          className={"form-field"}
          optionKey="code"
          selected={master && modulee ? toDropdownObj(master) : masterName}
          select={(e) => {
            setMasterName(e);
            setModuleName(null)
            setUpdatedConfig(null)
          }}
          t={t}
          // placeholder={t("WBH_MODULE_NAME")}
          placeholder={t("WBH_MODULE_NAME")}
          
          disable={master ? true : false}
        />
        <Dropdown
          option={moduleOptions}
          style={{width:"25%",marginRight:"auto" }}
          className={"form-field"}
          optionKey="code"
          selected={master && modulee ? toDropdownObj(master,modulee) : moduleName}
          select={(e) => {
            setModuleName(e);
          }}
          t={t}
          // placeholder={t("WBH_MODULE_NAME")}
          placeholder={t("WBH_MASTER_NAME")}
          
          disable = {modulee ? true : false}
        />
      </div>
    </React.Fragment>
  );
};

export default MDMSManageMaster;
