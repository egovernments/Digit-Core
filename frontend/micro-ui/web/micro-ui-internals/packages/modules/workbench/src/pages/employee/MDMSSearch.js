import { AddFilled, Button, Header, InboxSearchComposer, Loader } from "@egovernments/digit-ui-react-components";
import React, { useState, useEffect, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { useHistory, useParams } from "react-router-dom";
import { Config } from "../../configs/searchMDMSConfig";
import _, { drop } from "lodash"
// works-ui/employee/dss/search/commonMuktaUiConfig/SearchEstimateConfig
const MDMSSearch = () => {
  const { t } = useTranslation();
  const history = useHistory();
  const { moduleName, masterName } = Digit.Hooks.useQueryParams();
  // const [pageConfig, setPageConfig] = useState(null);
  const tenant = Digit.ULBService.getStateId();
  const { isLoading, data:ccConfig } = Digit.Hooks.useCustomMDMS(
    tenant,
    moduleName,
    [
      {
        name: masterName,
      },
    ],
    {
      select: (data) => {
        //response jsonPath will always be data.moduleName.masterName // this will be an array
        const response = _.get(data,`${moduleName}.${masterName}`,{})
        //take any row from response array and check which all fields are string
        const dropDownOptions=[] 
        Object.keys(response?.[0])?.forEach(key => {
          if(typeof response?.[0]?.[key] === "string"){
            dropDownOptions.push({
              name:key,
              code:key
            })
          }
          
        })
        
        Config.apiDetails.requestBody.MdmsCriteria.moduleDetails[0].moduleName = moduleName
        Config.apiDetails.requestBody.MdmsCriteria.moduleDetails[0].masterDetails[0].name = moduleName
        Config.sections.search.uiConfig.fields[0].populators.options = dropDownOptions
        return Config
        //here enrich config according to master and module(basically those dropdown options)
      },
    }
  );
  // const isLoading = false;
  // let data = Config || {};
  // let configs = data || {};

  // const {} = Digit.Hooks.useCustomMdmsv2()

  const updatedConfig = useMemo(() => Digit.Utils.preProcessMDMSConfigInboxSearch(t, ccConfig, "sections.search.uiConfig.fields", {
    updateDependent : [
      {
        key : "createdFrom",
        value : [new Date().toISOString().split("T")[0]]
      },
      {
        key : "createdTo",
        value : [new Date().toISOString().split("T")[0]]
      }
    ]
  }), [
    ccConfig,
  ]);

  // useEffect(() => {
  //   setPageConfig(_.cloneDeep(configs));
  // }, [data]);

  if (isLoading ) return <Loader />;

  return (
    <React.Fragment>
      <div className="jk-header-btn-wrapper">
        <Header styles={{ fontSize: "32px" }}>{t(updatedConfig?.label)}</Header>
        {Digit.Utils.didEmployeeHasRole(updatedConfig?.actionRole) && (
          <Button
            label={t(updatedConfig?.actionLabel)}
            variation="secondary"
            icon={<AddFilled style={{ height: "20px", width: "20px" }} />}
            onButtonClick={() => {
              history.push(`/${window?.contextPath}/employee/${updatedConfig?.actionLink}` + "?moduleName=common-masters&masterName=StateInfo1");
            }}
            type="button"
          />
        )}
      </div>
      <div className="inbox-search-wrapper">
        <InboxSearchComposer configs={updatedConfig}></InboxSearchComposer>
      </div>
    </React.Fragment>
  );
};

export default MDMSSearch;
