import React from "react";
import { formatter } from "../utils/formatter";
/* Amount component by default round offs and formats for amount   */

const Amount = ({ t, roundOff = true, ...props }) => {
  const value = roundOff ? Math.round(props?.value) : props?.value;
  return (
    <p className={`digit-amount ${props?.className ? props?.className : ""}`} style={props?.style}>
      {value ? `${formatter(value, "number")}` : t("ES_COMMON_NA")}
    </p>
  );
};

export default Amount;
