import { AddFilled, Button, Header, InboxSearchComposer, Loader, Dropdown } from "@egovernments/digit-ui-react-components";
import React, { useState, useEffect, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { useHistory, useParams } from "react-router-dom";
import _, { drop } from "lodash";
import { Config } from "../../configs/LocalisationSearchConfig";

const toDropdownObj = (master = "", mod = "") => {
  return {
    name: mod || master,
    code: Digit.Utils.locale.getTransformedLocale(mod ? `WBH_MDMS_${master}_${mod}` : `WBH_MDMS_MASTER_${master}`),
  };
};


const LocalisationSearch = () => {
  const { t } = useTranslation();
  const history = useHistory();
  const tenant = Digit.ULBService.getStateId();
  const tenantId = Digit.ULBService.getCurrentTenantId();
  
  
  
  // const { isLoading, data: dropdownData } = Digit.Hooks.useCustomAPIHook({
  //   url: "/mdms-v2/schema/v1/_search",
  //   params: {},
  //   body: {
      
  //   },
  //   config: {
  //     select: (data) => {
  //      return data
  //     },
  //   },
  // });


  // if (isLoading) return <Loader />;
  return (
    <React.Fragment>
      <Header className="works-header-search">{t(Config?.label)}</Header>
      
      {Config && <div className="inbox-search-wrapper">
        <InboxSearchComposer configs={Config}></InboxSearchComposer>
      </div>}
    </React.Fragment>
  );
};

export default LocalisationSearch;
