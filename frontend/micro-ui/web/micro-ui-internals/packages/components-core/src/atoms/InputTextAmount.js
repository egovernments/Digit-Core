import React, { useEffect, useState } from "react";
import PropTypes from "prop-types";
import { getIntlConfig } from "../utils/amountFormatter";
import { InputAmountWrapper } from "../utils/inputAmountWrapper";

const InputTextAmount = ({ value, prefix = "₹ ", intlConfig = getIntlConfig(prefix), onChange, inputRef, variant, ...otherProps }) => {
  return (
    <InputAmountWrapper
      ref={inputRef}
      defaultValue={value}
      intlConfig={intlConfig}
      onValueChange={onChange}
      otherProps={otherProps}
      prefix={prefix}
      variant={variant}
    />
  );
};

InputTextAmount.propTypes = {
  value: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
  prefix: PropTypes.string,
  intlConfig: PropTypes.object,
  onChange: PropTypes.func,
  inputRef: PropTypes.oneOfType([PropTypes.func, PropTypes.shape({ current: PropTypes.instanceOf(Element) })]),
  otherProps: PropTypes.object,
};

export default InputTextAmount;
