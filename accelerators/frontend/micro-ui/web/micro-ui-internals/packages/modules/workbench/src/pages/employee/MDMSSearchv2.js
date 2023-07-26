import { AddFilled, Button, Header, InboxSearchComposer, Loader, Dropdown } from "@egovernments/digit-ui-react-components";
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
};

const sampleSchemaResponse = {
  ResponseInfo: null,
  SchemaDefinitions: [
    {
      id: "aa4d8d08-658b-45c1-af4d-d57428fa5e52",
      tenantId: "pg",
      code: "common-masters.Sample",
      description: "Sample Master",
      definition: {
        type: "object",
        title: "Generated schema for Root",
        $schema: "http://json-schema.org/draft-07/schema#",
        required: ["name", "code"],
        "x-unique": ["code"],
        properties: {
          id: {
            type: "string",
          },
          code: {
            type: "string",
          },
          name: {
            type: "string",
          },
          active: {
            type: "boolean",
          },
          d: {
            type: "string",
          },
        },
      },
      isActive: true,
      auditDetails: {
        createdBy: null,
        lastModifiedBy: null,
        createdTime: 1689914236639,
        lastModifiedTime: 1689914236639,
      },
    },
    {
      id: "aa4d8d08-658b-45c1-af4d-d57428fa5e52",
      tenantId: "pg",
      code: "common-masters.SampleTwo",
      description: "Sample Master",
      definition: {
        type: "object",
        title: "Generated schema for Root",
        $schema: "http://json-schema.org/draft-07/schema#",
        required: ["name", "code"],
        "x-unique": ["code"],
        properties: {
          id: {
            type: "string",
          },
          code: {
            type: "string",
          },
          name: {
            type: "string",
          },
          active: {
            type: "boolean",
          },
          d: {
            type: "string",
          },
        },
      },
      isActive: true,
      auditDetails: {
        createdBy: null,
        lastModifiedBy: null,
        createdTime: 1689914236639,
        lastModifiedTime: 1689914236639,
      },
    },
    {
      id: "aa4d8d08-658b-45c1-af4d-d57428fa5e52",
      tenantId: "pg",
      code: "common-masters.Simple",
      description: "Simple Master",
      definition: {
        type: "object",
        title: "Generated schema for Root",
        $schema: "http://json-schema.org/draft-07/schema#",
        required: ["name", "code"],
        "x-unique": ["code"],
        properties: {
          id: {
            type: "string",
          },
          code: {
            type: "string",
          },
          name: {
            type: "string",
          },
          active: {
            type: "boolean",
          },
          desc: {
            type: "string",
          },
        },
      },
      isActive: true,
      auditDetails: {
        createdBy: null,
        lastModifiedBy: null,
        createdTime: 1689914236639,
        lastModifiedTime: 1689914236639,
      },
    },
    {
      id: "aa4d8d08-658b-45c1-af4d-d57428fa5e52",
      tenantId: "pg",
      code: "common-masters.Department",
      description: "Sample Master",
      definition: {
        type: "object",
        title: "Generated schema for Root",
        $schema: "http://json-schema.org/draft-07/schema#",
        required: ["name", "code"],
        "x-unique": ["code"],
        properties: {
          id: {
            type: "string",
          },
          code: {
            type: "string",
          },
          name: {
            type: "string",
          },
          active: {
            type: "boolean",
          },
          de: {
            type: "string",
          },
        },
      },
      isActive: true,
      auditDetails: {
        createdBy: null,
        lastModifiedBy: null,
        createdTime: 1689914236639,
        lastModifiedTime: 1689914236639,
      },
    },
    {
      id: "aa4d8d08-658b-45c1-af4d-d57428fa5e52",
      tenantId: "pg",
      code: "works.Category",
      description: "Sample Master",
      definition: {
        type: "object",
        title: "Generated schema for Root",
        $schema: "http://json-schema.org/draft-07/schema#",
        required: ["name", "code"],
        "x-unique": ["code"],
        properties: {
          id: {
            type: "string",
          },
          code: {
            type: "string",
          },
          name: {
            type: "string",
          },
          active: {
            type: "boolean",
          },
          des: {
            type: "string",
          },
        },
      },
      isActive: true,
      auditDetails: {
        createdBy: null,
        lastModifiedBy: null,
        createdTime: 1689914236639,
        lastModifiedTime: 1689914236639,
      },
    },
    {
      id: "aa4d8d08-658b-45c1-af4d-d57428fa5e52",
      tenantId: "pg",
      code: "works.BeneficiaryType",
      description: "Sample Master",
      definition: {
        type: "object",
        title: "Generated schema for Root",
        $schema: "http://json-schema.org/draft-07/schema#",
        required: ["name", "code"],
        "x-unique": ["code"],
        properties: {
          id: {
            type: "string",
          },
          code: {
            type: "string",
          },
          name: {
            type: "string",
          },
          active: {
            type: "boolean",
          },
          desc: {
            type: "string",
          },
        },
      },
      isActive: true,
      auditDetails: {
        createdBy: null,
        lastModifiedBy: null,
        createdTime: 1689914236639,
        lastModifiedTime: 1689914236639,
      },
    },
  ],
};

const MDMSSearchv2 = () => {
  let Config = _.clone(Configg)
  const { t } = useTranslation();
  const history = useHistory();
  const tenant = Digit.ULBService.getStateId();

  const [availableSchemas, setAvailableSchemas] = useState([]);
  const [currentSchema, setCurrentSchema] = useState(null);
  const [masterName, setMasterName] = useState(null); //for dropdown
  const [moduleName, setModuleName] = useState(null); //for dropdown
  const [masterOptions,setMasterOptions] = useState([])
  const [moduleOptions,setModuleOptions] = useState([])
  const [updatedConfig,setUpdatedConfig] = useState(null)
  const { isLoading, data: dropdownData } = Digit.Hooks.useCustomAPIHook({
    url: "/mdms-v2/schema/v1/_search",
    params: {},
    body: {
      SchemaDefCriteria: {
        tenantId: "pg",
        codes: ["common-masters.Sample"],
      },
    },
    config: {
      select: (data) => {
        function onlyUnique(value, index, array) {
          return array.indexOf(value) === index;
        }
        //when api is working fine change here(thsese are all schemas available in a tenant)
        const schemas = sampleSchemaResponse.SchemaDefinitions;
        setAvailableSchemas(schemas);
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
        if (typeof properties[key].type === "string") {
          dropDownOptions.push({
            name: key,
            code: key,
          });
        }
      });
console.log("currentSchema",currentSchema);
      Config.sections.search.uiConfig.fields[0].populators.options = dropDownOptions;
      Config.actionLink=Config.actionLink+`?moduleName=${masterName?.name}&masterName=${moduleName?.name}`;
      setUpdatedConfig(Config)
    }
  }, [currentSchema]);

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
          selected={masterName}
          select={(e) => {
            setMasterName(e);
            setModuleName(null)
            setUpdatedConfig(null)
          }}
          t={t}
          placeholder={t("WBH_MODULE_NAME")}
        />
        <Dropdown
          option={moduleOptions}
          style={{width:"25%",marginRight:"auto" }}
          className={"form-field"}
          optionKey="code"
          selected={moduleName}
          select={(e) => {
            setModuleName(e);
          }}
          t={t}
          placeholder={t("WBH_MODULE_NAME")}
        />
       {updatedConfig && moduleName && masterName && Digit.Utils.didEmployeeHasRole(updatedConfig?.actionRole) && (
          <Button
            label={t(updatedConfig?.actionLabel)}
            variation="secondary"
            icon={<AddFilled style={{ height: "20px", width: "20px" }} />}
            onButtonClick={() => {
              history.push(`/${window?.contextPath}/employee/${updatedConfig?.actionLink}`);
            }}
            type="button"
          />
        )}
      </div>
      {updatedConfig && moduleName && masterName && <div className="inbox-search-wrapper">
        <InboxSearchComposer configs={updatedConfig}></InboxSearchComposer>
      </div>}
    </React.Fragment>
  );
};

export default MDMSSearchv2;
