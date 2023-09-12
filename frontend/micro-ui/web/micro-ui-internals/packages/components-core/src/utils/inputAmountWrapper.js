import React, { useEffect, useState } from "react";
import TextInput from "../atoms/TextInput";
import { CurrencyInput } from "./currencyInput";

const DEFAULT_PREFIX = "₹ ";
const LIMIT = 500;

export const InputAmountWrapper = ({ ref, ...props }) => {
  const prefix = props?.prefix || DEFAULT_PREFIX;

  const [errorMessage, setErrorMessage] = useState("");
  const [className, setClassName] = useState("");
  const [value, setValue] = useState(props?.defaultValue);
  const [values, setValues] = useState();
  const [rawValue, setRawValue] = useState(" ");

  useEffect(() => {
    props.onValueChange ? props.onValueChange(value) : null;
  }, [value]);

  /**
   * Handle validation
   */
  const handleOnValueChange = (value, _, values) => {
    setValues(values);
    setRawValue(value === undefined ? "undefined" : value || " ");

    if (!value) {
      setClassName("");
      setValue("");
      return;
    }

    if (Number.isNaN(Number(value))) {
      // setErrorMessage('Please enter a valid number');
      // setClassName('is-invalid');
      return;
    }

    if (Number(value) > LIMIT) {
      // setErrorMessage(`Max: ${prefix}${LIMIT}`);
      // setClassName('is-invalid');
      setValue(value);
      return;
    }

    // setClassName('is-valid');
    setValue(value);
  };

  return (
    <CurrencyInput
      id="validationCustom01"
      name="input-1"
      customInput={TextInput}
      className={`form-control ${className} ${props?.variant}`}
      value={props.defaultValue ? props.defaultValue : value ? value : ""}
      onValueChange={handleOnValueChange}
      // placeholder="Please enter a number"
      prefix={prefix}
      step={1}
      otherProps={props?.otherProps}
      inputRef={ref}
    />
  );
};

export default InputAmountWrapper;
