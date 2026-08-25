import React, { useMemo, useState } from "react";
import Select from "react-select";
import { useTranslation } from "react-i18next";


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
  const { t } = useTranslation();
  const { moduleName, masterName } = Digit.Hooks.useQueryParams();
  const { options, value, disabled, readonly, onChange, onBlur, onFocus, placeholder, multiple = false ,schema={schemaCode:""}} = props;
  const {schemaCode=`${moduleName}.${masterName}`} = schema;
  const handleChange = (selectedValue) => onChange(multiple ? selectedValue?.value : selectedValue?.value);
  const optionsList = options?.enumOptions || options || [];
  const formattedOptions=React.useMemo(()=>optionsList.map(e=>({label:t(Digit.Utils.locale.getTransformedLocale(`${schemaCode}_${e?.label}`)),value:e.value})),[optionsList,schemaCode]);
  const selectedOption = formattedOptions?.filter((obj) => (multiple ? value?.includes(obj.value) : obj.value == value));
 
  return (
    <Select
      className="form-control form-select"
      classNamePrefix="digit"
      options={formattedOptions}
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
