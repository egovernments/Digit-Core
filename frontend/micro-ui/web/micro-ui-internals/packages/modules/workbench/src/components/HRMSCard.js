import { HRIcon, EmployeeModuleCard } from "@egovernments/digit-ui-react-components";
import React from "react";
import { useTranslation } from "react-i18next";

const HRMSCard = () => {
  const ADMIN = Digit.Utils.hrmsAccess();
  if (!ADMIN) {
    return null;
  }
  const { t } = useTranslation();
  // const tenantId = Digit.ULBService.getCurrentTenantId();
  // const { isLoading, isError, error, data, ...rest } = Digit.Hooks.hrms.useHRMSCount(tenantId);

  const propsForModuleCard = {
    Icon: <HRIcon />,
    moduleName: t("ACTION_TEST_9HRMS"),
    kpis: [
      // {
      //     count:  isLoading ? "-" : data?.EmployeCount?.totalEmployee,
      //     label: t("TOTAL_EMPLOYEES"),
      //     link: `/${window?.contextPath}/employee/hrms/inbox`
      // },
      // {
      //   count:  isLoading ? "-" : data?.EmployeCount?.activeEmployee,
      //     label: t("ACTIVE_EMPLOYEES"),
      //     link: `/${window?.contextPath}/employee/hrms/inbox`
      // }
    ],
    links: [
      {
        label: t("HR_HOME_SEARCH_RESULTS_HEADING"),
        link: `/${window?.contextPath}/employee/hrms/inbox`,
      },
      {
        label: t("HR_COMMON_CREATE_EMPLOYEE_HEADER"),
        link: `/${window?.contextPath}/employee/hrms/create`,
      },
    ],
  };

  return <EmployeeModuleCard {...propsForModuleCard} />;
};

export default HRMSCard;
