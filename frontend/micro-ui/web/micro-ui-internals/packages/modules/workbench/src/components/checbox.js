import { ToggleSwitch } from "@egovernments/digit-ui-react-components";
import React from "react";
import { useTranslation } from "react-i18next";


export const CustomCheckbox = function (props) {
  const { t } = useTranslation();

  return (
    <div onClick={() => props.onChange(!props.value)} className="custom-checkbox">
      {props.value && <ToggleSwitch value={true}></ToggleSwitch>}
      {!props.value && <ToggleSwitch value={false}></ToggleSwitch>}
      <span className="custom-checkbox-label">{props.value ? t("WBH_BOOLEAN_VALUE_TRUE") :  t("WBH_BOOLEAN_VALUE_FALSE")}</span>
    </div>
  );
};