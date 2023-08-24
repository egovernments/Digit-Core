import React, { useState } from "react";
import Select from "react-select";

const customStyles = {
  control: (provided, state) => ({
    ...provided,
    borderColor: state.isFocused ? '#f47738' : '#505a5f',
    borderRadius:'unset',
    '&:hover': {
      borderColor: '#f47738',
    },
  }),
};

/* Multiple support not added TODO jagan to fix this issue */
const CustomSelectWidget = (props) => {
  const { options, value, disabled, readonly, onChange, onBlur, onFocus, placeholder, multiple = false } = props;
  const handleChange = (selectedValue) => onChange(multiple ? selectedValue?.value : selectedValue?.value);
  const optionsList = options?.enumOptions || options || [];
  const selectedOption = optionsList?.filter((obj) => (multiple ? value?.includes(obj.value) : obj.value == value));
  return (
    <Select
      className="form-control form-select"
      classNamePrefix="digit"
      options={optionsList}
      isDisabled={disabled || readonly}
      placeholder={placeholder}
      onBlur={onBlur}
      onFocus={onFocus}
      closeMenuOnScroll={true}
      value={selectedOption}
      onChange={handleChange}
      isSearchable={true}
      isMulti={multiple}
      styles={customStyles}

    />
  );
};
export default CustomSelectWidget;
