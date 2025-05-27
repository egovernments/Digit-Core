import { AddFilled, Button, Header, InboxSearchComposer, Loader, Dropdown,SubmitBar, ActionBar } from "@egovernments/digit-ui-react-components";
import React, { useState, useEffect, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { useHistory, useParams } from "react-router-dom";
import { Config as Configg } from "../../configs/searchMDMSConfig";
import _, { drop } from "lodash";

const toDropdownObj = (master = "", mod = "") => {
  return {
    name: mod || master,
    code: Digit.Utils.locale.getTransformedLocale(mod ? `WBH_MDMS_${master}_${mod}` : `WBH_MDMS_MASTER_${master}`),
  };

  // return {
  //   name: mod || master,
  //   code: mod ? `${mod}` : `${master}`,
  // };
};


const MDMSSearchv2 = () => {
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
        obj.mastersAvailable = obj.mastersAvailable.filter(onlyUnique);
        obj.mastersAvailable = obj.mastersAvailable.map((mas) => toDropdownObj(mas));

        return obj;
      },
    },
  });


  useEffect(() => {
    setMasterOptions(dropdownData?.mastersAvailable)
  }, [dropdownData])

  useEffect(() => {
    setModuleOptions(dropdownData?.[masterName?.name])
  }, [masterName])

  useEffect(() => {
    //here set current schema based on module and master name
    if(masterName?.name && moduleName?.name){
    setCurrentSchema(availableSchemas.filter(schema => schema.code === `${masterName?.name}.${moduleName?.name}`)?.[0])
    }
  }, [moduleName])
  
  useEffect(() => {
    if (currentSchema) {
      const dropDownOptions = [];
      const {
        definition: { properties },
      } = currentSchema;
      
      Object.keys(properties)?.forEach((key) => {
        if (properties[key].type === "string" && !properties[key].format) {
          dropDownOptions.push({
            // name: key,
            name:key,
            code: key,
            i18nKey:Digit.Utils.locale.getTransformedLocale(`${currentSchema.code}_${key}`)
          });
        }
      });

      Config.sections.search.uiConfig.fields[0].populators.options = dropDownOptions;
      Config.actionLink=Config.actionLink+`?moduleName=${masterName?.name}&masterName=${moduleName?.name}`;
      // Config.apiDetails.serviceName = `/mdms-v2/v2/_search/${currentSchema.code}`
      
      
      Config.additionalDetails = {
        currentSchemaCode:currentSchema.code
      }
      //set the column config
      
      // Config.sections.searchResult.uiConfig.columns = [{
      //   label: "WBH_UNIQUE_IDENTIFIER",
      //   jsonPath: "uniqueIdentifier",
      //   additionalCustomization:true
      // },...dropDownOptions.map(option => {
      //   return {
      //     label:option.i18nKey,
      //     i18nKey:option.i18nKey,
      //     jsonPath:`data.${option.code}`,
      //     dontShowNA:true
      //   }
      // })]

      Config.sections.searchResult.uiConfig.columns = [...dropDownOptions.map(option => {
        return {
          label:option.i18nKey,
          i18nKey:option.i18nKey,
          jsonPath:`data.${option.code}`,
          dontShowNA:true
        }
      }),{
        label:"WBH_ISACTIVE",
        i18nKey:"WBH_ISACTIVE",
        jsonPath:`isActive`,
        additionalCustomization:true
        // dontShowNA:true
      }]

      setUpdatedConfig(Config)
    }
  }, [currentSchema]);

  const handleAddMasterData = () => {
    let actionLink=updatedConfig?.actionLink
    if(modulee&&master){
      actionLink= `workbench/mdms-add-v2?moduleName=${master}&masterName=${modulee}`
    }
    history.push(`/${window?.contextPath}/employee/${actionLink}`);
  }

  const onClickRow = ({original:row}) => {
    const [moduleName,masterName] = row.schemaCode.split(".")
    history.push(`/${window.contextPath}/employee/workbench/mdms-view?moduleName=${moduleName}&masterName=${masterName}&uniqueIdentifier=${row.uniqueIdentifier}`)
  }

  if (isLoading) return <Loader />;
  return (
    <React.Fragment>
        {/* <Header className="works-header-search">{t(Config?.label)}</Header> */}
      <Header className="digit-form-composer-sub-header">{t(Digit.Utils.workbench.getMDMSLabel(`SCHEMA_` + currentSchema?.code))}</Header>
      {/* <div className="jk-header-btn-wrapper">
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
       {updatedConfig && Digit.Utils.didEmployeeHasRole(updatedConfig?.actionRole) && (
          <Button
            label={t(updatedConfig?.actionLabel)}
            variation="secondary"
            icon={<AddFilled style={{ height: "20px", width: "20px" }} />}
            onButtonClick={() => {
              let actionLink=updatedConfig?.actionLink
              if(modulee&&master){
                actionLink= `workbench/mdms-add-v2?moduleName=${master}&masterName=${modulee}`
              }
              history.push(`/${window?.contextPath}/employee/${actionLink}`);
            }}
            type="button"
          />
        )}
      </div> */}
      {
        updatedConfig && Digit.Utils.didEmployeeHasRole(updatedConfig?.actionRole) &&
        <ActionBar >
          <SubmitBar disabled={false} onSubmit={handleAddMasterData} label={t("WBH_ADD_MDMS")} />
        </ActionBar>
      }
      {updatedConfig && <div className="inbox-search-wrapper">
        <InboxSearchComposer configs={updatedConfig} additionalConfig = {{
          resultsTable:{
            onClickRow
          }
        }}></InboxSearchComposer>
      </div>}
    </React.Fragment>
  );
};

export default MDMSSearchv2;
