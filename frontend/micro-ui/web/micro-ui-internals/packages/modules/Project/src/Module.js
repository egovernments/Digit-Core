import { Loader} from "@egovernments/digit-ui-react-components";
import React from "react";
import { useRouteMatch } from "react-router-dom";
import { default as DemoPackage } from "./pages/demopackage";
import ProjectCard from "./components/ProjectCard";
import ProjectComponent from "./components/ProjectComponent";

export const ProjectModule = ({ stateCode, userType, tenants }) => {
  const { path, url } = useRouteMatch();
  const tenantId = Digit.ULBService.getCurrentTenantId();
  const moduleCode = ["sample", "common","workflow", tenantId];
  const language = Digit.StoreData.getCurrentLanguage();
  const { isLoading, data: store } = Digit.Services.useStore({
    stateCode,
    moduleCode,
    language,
  });

  if (isLoading) {
    return <Loader />;
  }
  return <DemoPackage path={path} stateCode={stateCode} userType={userType} tenants={tenants} />;
};

const componentsToRegister = {
    ProjectModule,
    ProjectCard,
    ProjectComponent
};
//init <modulename >component
export const initProjectComponents = () => {
  Object.entries(componentsToRegister).forEach(([key, value]) => {
    Digit.ComponentRegistryService.setComponent(key, value);
  });
};
