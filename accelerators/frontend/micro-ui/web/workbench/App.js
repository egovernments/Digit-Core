import React from "react";
import { initLibraries } from "@egovernments/digit-ui-libraries";
import { DigitUI } from "@egovernments/digit-ui-module-core";
import { initHRMSComponents } from "@egovernments/digit-ui-module-hrms";
import { initUtilitiesComponents } from  "@egovernments/digit-ui-module-utilities";
import { UICustomizations } from "./Customisations/UICustomizations";

window.contextPath = window?.globalConfigs?.getConfig("CONTEXT_PATH");

const enabledModules = ["DSS", "NDSS",  "Utilities",
"HRMS", "Engagement"
];
window.Digit.ComponentRegistryService.setupRegistry({

});


initHRMSComponents();
initUtilitiesComponents();

const moduleReducers = (initData) => ({
  initData,
});


const initDigitUI = () => {
  window.Digit.ComponentRegistryService.setupRegistry({});

  initHRMSComponents();
  initUtilitiesComponents();

 
  window.Digit.Customizations = {
    PGR: {},
    commonUiConfig: UICustomizations,
  };
};

initLibraries().then(() => {
  initDigitUI();
});

function App() {
  window.contextPath = window?.globalConfigs?.getConfig("CONTEXT_PATH");
  const stateCode =
    window.globalConfigs?.getConfig("STATE_LEVEL_TENANT_ID") ||
    process.env.REACT_APP_STATE_LEVEL_TENANT_ID;
  if (!stateCode) {
    return <h1>stateCode is not defined</h1>;
  }
  return (
    <DigitUI
      stateCode={stateCode}
      enabledModules={enabledModules}
      moduleReducers={moduleReducers}
      // defaultLanding="employee"
    />
  );
}

export default App;
