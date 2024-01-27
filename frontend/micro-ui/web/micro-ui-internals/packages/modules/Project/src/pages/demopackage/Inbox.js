import React, { useEffect, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { Header, InboxSearchComposer, Loader, Button, AddFilled } from "@egovernments/digit-ui-react-components";
import { useHistory, useLocation } from "react-router-dom";
import inboxConfig from "../../../../../modules/sample/src/configs/inboxConfig";
const SearchWageSeeker = () => {
  const { t } = useTranslation();
  const history = useHistory()
  const location = useLocation()


  var demoObject=  {
    "RequestInfo": {
      "authToken": "3c32223d-addb-4506-af5d-58ff6705b728"
    },
    "tenantId":"mz",
    "HouseholdMember": {
        "id": [
             "0071c052-c7ff-4fce-82aa-cb28aa4685db"
        ],
            "householdId": "H-2023-11-07-009079",
         "householdClientReferenceId": "49d777d4-5f1a-45ec-b211-a59c6a18bcae",
            "individualId": "699220a8-f500-418d-9568-a470727506e1",
            "individualClientReferenceId": "394ba865-d608-43f8-8ec5-742fce037020",
        "isHeadOfHousehold": true
    }
}

  

  const democonfig = inboxConfig()

  console.log("config detail",democonfig)

  const updatedConfig = useMemo(
    () => Digit.Utils.preProcessMDMSConfigInboxSearch(t, democonfig,"sections.search.uiConfig.fields",{}),
    [democonfig]);

    console.log("updated config is", updatedConfig)

    

  return (
    <React.Fragment>
      <div className="inbox-search-wrapper">
        <InboxSearchComposer configs={updatedConfig}></InboxSearchComposer>
      </div>
    </React.Fragment>
  );
};

export default SearchWageSeeker;
