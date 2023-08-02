import React, { useState } from "react";
import Select from "react-select";

/* Multiple support not added TODO jagan to fix this issue */
const CustomSelectWidget = (props) => {
  const { options, value, disabled, readonly, onChange, onBlur, onFocus, placeholder, multiple = false } = props;
  const handleChange = (selectedValue) => onChange(multiple ? selectedValue?.value : selectedValue?.value);
  const optionsList = options?.enumOptions || options || [];
  const selectedOption = optionsList?.filter((obj) => (multiple ? value?.includes(obj.value) : obj.value == value));
  return (
    <Select
      className="form-control"
      classNamePrefix="digit"
      options={optionsList}
      disabled={disabled || readonly}
      placeholder={placeholder}
      onBlur={onBlur}
      onFocus={onFocus}
      value={selectedOption}
      onChange={handleChange}
      isSearchable={true}
      isMulti={multiple}
    />
  );
};
export default CustomSelectWidget;
