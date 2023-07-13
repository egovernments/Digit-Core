import { EmployeeModuleCard, ArrowRightInbox, WorksMgmtIcon } from "@egovernments/digit-ui-react-components";
import React from "react";
import { useTranslation } from "react-i18next";

const ROLES = {
  LOCALISATION: ["EMPLOYEE", "PROJECT_VIEWER"],
  ESTIMATE: ["ESTIMATE_CREATOR", "ESTIMATE_VERIFIER", "TECHNICAL_SANCTIONER", "ESTIMATE_APPROVER"],
  CONTRACT: ["WORK_ORDER_CREATOR", "WORK_ORDER_VERIFIER", "WORK_ORDER_APPROVER"],
  MASTERS: ["MUKTA_ADMIN"],
  BILLS: ["BILL_CREATOR", "BILL_VERIFIER","BILL_APPROVER"],
  PAYMENT: ["BILL_ACCOUNTANT"],
  MUSTERROLLS: ["MUSTER_ROLL_VERIFIER", "MUSTER_ROLL_APPROVER"],
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
      label: t("Localisation"),
      link: `/${window?.contextPath}/employee/project/search-project`,
      roles: ROLES.LOCALISATION,
    },
    {
      label: t("Manage Master Data"),
      link: `/${window?.contextPath}/employee/estimate/inbox`,
      roles: ROLES.ESTIMATE,
    },

  ];

  links = links.filter((link) => (link?.roles && link?.roles?.length > 0 ? Digit.Utils.didEmployeeHasAtleastOneRole(link?.roles) : true));

  const propsForModuleCard = {
    Icon: <WorksMgmtIcon />,
    moduleName: t("Workbench"),
    kpis: [
    ],
    links: links,
  };
  return <EmployeeModuleCard {...propsForModuleCard} />;
};

export default WorkbenchCard;

