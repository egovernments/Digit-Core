import React, { useEffect } from "react";
import { Switch, useLocation } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { PrivateRoute, AppContainer, BreadCrumb } from "@egovernments/digit-ui-react-components";
import LocalisationSearch from "./LocalisationSearch";
import MDMSSearch from "./MDMSSearch";
import MDMSAdd from "./MDMSAdd";
import MDMSAddV2 from "./MDMSAddV2";

import MDMSSearchv2 from "./MDMSSearchv2";
const MastersBreadCrumb = ({ location ,defaultPath}) => {
  const { t } = useTranslation();
  const search = useLocation().search;
  const fromScreen = new URLSearchParams(search).get("from") || null;
  const pathVar=location.pathname.replace(defaultPath+'/',"");

  const crumbs = [
    {
      path: `/${window?.contextPath}/employee`,
      content: t("WORKS_MUKTA"),
      show: true,
    },
    {
      path: `/${window.contextPath}/employee/masters/response`,
      content:  t(`${Digit.Utils.workbench.getMDMSLabel(pathVar)}`) ,
      show: true,
    },
  ];
  return <BreadCrumb crumbs={crumbs} spanStyle={{ maxWidth: "min-content" }} />;
};

const App = ({ path }) => {
  const location = useLocation();
  const MDMSCreateSession = Digit.Hooks.useSessionStorage("MDMS_CREATE", {});
  const [sessionFormData, setSessionFormData, clearSessionFormData] = MDMSCreateSession;
  useEffect(() => {
    if (!window.location.href.includes("mdms-add") && sessionFormData && Object.keys(sessionFormData) != 0) {
      clearSessionFormData();
    }
  }, [location]);
  return (
    <React.Fragment>
      <MastersBreadCrumb location={location} defaultPath={path} />
      <Switch>
        <div>
          <PrivateRoute path={`${path}/sample`} component={() => <div>Sample Screen loaded</div>} />
          <PrivateRoute path={`${path}/localisation-search`} component={() => <LocalisationSearch />} />
          <PrivateRoute path={`${path}/mdms-search`} component={() => <MDMSSearch />} />
          <PrivateRoute path={`${path}/mdms-add`} component={() =>  <MDMSAdd FormSession={MDMSCreateSession} parentRoute={path}/>} />
          
          <PrivateRoute path={`${path}/mdms-add-v2`} component={() =>  <MDMSAddV2 FormSession={MDMSCreateSession} parentRoute={path}/>} />
          <PrivateRoute path={`${path}/mdms-search-v2`} component={() => <MDMSSearchv2 />} />
        </div>
      </Switch>
    </React.Fragment>
  );
};

export default App;
