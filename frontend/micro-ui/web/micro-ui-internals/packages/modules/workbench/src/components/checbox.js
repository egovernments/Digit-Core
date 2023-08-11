import { ToggleSwitch } from "@egovernments/digit-ui-react-components";
import React from "react";
export const CustomCheckbox = function (props) {
  return (
    <div onClick={() => props.onChange(!props.value)} className="custom-checkbox">
      {props.value && <ToggleSwitch value={true}></ToggleSwitch>}
      {!props.value && <ToggleSwitch value={false}></ToggleSwitch>}
      <span className="custom-checkbox-label">{props.value ? "True" : "False"}</span>
    </div>
  );
};
