import { EmployeeModuleCard, ArrowRightInbox, WorksMgmtIcon } from "@egovernments/digit-ui-react-components";
import React from "react";
import { useTranslation } from "react-i18next";

const ROLES = {
  LOCALISATION: ["EMPLOYEE", "SUPERUSER","EMPLOYEE_COMMON","LOC_ADMIN"],
  MDMS: ["MDMS_ADMIN", "EMPLOYEE", "SUPERUSER"],
  DSS: ["STADMIN"],
};

// Mukta Overrriding the Works Home screen card
const WorkbenchCard = () => {
  if (!Digit.Utils.didEmployeeHasAtleastOneRole(Object.values(ROLES).flatMap((e) => e))) {
    return null;
  }

  const { t } = useTranslation();
  const tenantId = Digit.ULBService.getCurrentTenantId();

  let links = [
    {
      label: t("ACTION_TEST_MDMS"),
      link: `/${window?.contextPath}/employee/workbench/manage-master-data`,
      roles: ROLES.MDMS,
    },
    {
      label: t("ACTION_TEST_LOCALISATION"),
      link: `/${window?.contextPath}/employee/workbench/localisation-search`,
      roles: ROLES.LOCALISATION,
    },
    // {
    //   label: t("Sample Create master"),
    //   link: `/${window?.contextPath}/employee/workbench/mdms-add-v2?moduleName=common-masters&masterName=Sample`,
    //   roles: ROLES.MDMS,
    // },
    // {
    //   label: t("Sample Search master"),
    //   link: `/${window?.contextPath}/employee/workbench/mdms-search-v2?masterName=common-masters&moduleName=Sample`,
    //   roles: ROLES.MDMS,
    // },
  ];

  links = links.filter((link) => (link?.roles && link?.roles?.length > 0 ? Digit.Utils.didEmployeeHasAtleastOneRole(link?.roles) : true));

  const propsForModuleCard = {
    Icon: <WorksMgmtIcon />,
    moduleName: t("ACTION_TEST_WORKBENCH"),
    kpis: [],
    links: links,
  };
  return <EmployeeModuleCard {...propsForModuleCard} />;
};

export default WorkbenchCard;
