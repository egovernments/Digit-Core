import React, { useMemo } from "react";
import { useTranslation } from "react-i18next";
import { Route, Switch, useRouteMatch } from "react-router-dom";
import { loginConfig as defaultLoginConfig } from "./config";
import LoginComponent from "./login";

const EmployeeLogin = () => {
  const { t } = useTranslation();
  const { path } = useRouteMatch();

  const { data: mdmsData } = Digit.Hooks.useCommonMDMS(Digit.ULBService.getStateId(), "commonUiConfig", ["LoginConfig"], {
    select: (data) => {
      return {
        config: data?.MdmsRes?.['commonUiConfig']?.LoginConfig
      };
    },
    retry: false,
    enable: false,
  });


  let loginConfig = mdmsData?.config ? mdmsData?.config : defaultLoginConfig;

  const loginParams = useMemo(() =>
    loginConfig.map(
      (step) => {
        const texts = {};
        for (const key in step.texts) {
          texts[key] = t(step.texts[key]);
        }
        return { ...step, texts };
      },
      [loginConfig]
    )
  );

  return (
    <Switch>
      <Route path={`${path}`} exact>
        <LoginComponent config={loginParams[0]} t={t} />
      </Route>
    </Switch>
  );
};

export default EmployeeLogin;
