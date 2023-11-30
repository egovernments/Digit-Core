import { HRIcon, EmployeeModuleCard, AttendanceIcon, PropertyHouse } from "@egovernments/digit-ui-react-components";
import React from "react";
import { useTranslation } from "react-i18next";

const ProjectCard = () => {
 
  const { t } = useTranslation();

  const propsForModuleCard = {
    Icon: <PropertyHouse />,
    moduleName: t("new card"),
    kpis: [

    ],
    links: [
   
      {
        label: t("create"),
        link: `/${window?.contextPath}/employee/project/searchdemo`,
      },
      {
        label: t("inbox"),
        link: `/${window?.contextPath}/employee/project/inboxdemo`,
      },
      
      
    ],
  };

  return <EmployeeModuleCard {...propsForModuleCard} />;
};

export default ProjectCard;