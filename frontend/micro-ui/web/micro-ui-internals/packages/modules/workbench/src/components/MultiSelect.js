import React, { useMemo, useState } from "react";
import Select from "react-select";
import { useTranslation } from "react-i18next";
import { Loader } from "@egovernments/digit-ui-react-components";

const customStyles = {
  control: (provided, state) => ({
    ...provided,
    borderColor: state.isFocused ? "#f47738" : "#505a5f",
    borderRadius: "unset",
    "&:hover": {
      borderColor: "#f47738",
    },
  }),
};

/* Multiple support not added TODO jagan to fix this issue */
const CustomSelectWidget = (props) => {
  const { t } = useTranslation();
  const { moduleName, masterName } = Digit.Hooks.useQueryParams();
  const {
    options,
    value,
    disabled,
    readonly,
    onChange,
    onBlur,
    onFocus,
    placeholder,
    multiple = false,
    schema = { schemaCode: "", fieldPath: "" },
  } = props;
  const { schemaCode = `${moduleName}.${masterName}`, tenantId, fieldPath } = schema;
  const handleChange = (selectedValue) => onChange(multiple ? selectedValue?.value : selectedValue?.value);
  /*
  logic added to fetch data of schemas in each component itself
  */
  const reqCriteriaForData = {
    url: `/${Digit.Hooks.workbench.getMDMSContextPath()}/v2/_search`,
    params: {},
    body: {
      MdmsCriteria: {
        tenantId: tenantId,
        schemaCode: schemaCode,
      },
    },
    config: {
      enabled: schemaCode && schemaCode?.length > 0,
      select: (data) => {
        const respData = data?.mdms?.map((e) => ({ label: e?.uniqueIdentifier, value: e?.uniqueIdentifier }));
        const finalJSONPath = `registry.rootSchema.properties.${Digit.Utils.workbench.getUpdatedPath(fieldPath)}.enum`;
        if (_.has(props, finalJSONPath)) {
          _.set(
            props,
            finalJSONPath,
            respData?.map((e) => e.value)
          );
        }
        return respData;
      },
    },
    changeQueryName: `data-${schemaCode}`,
  };

  const { isLoading, data } = Digit.Hooks.useCustomAPIHook(reqCriteriaForData);
  const optionsList = data || options?.enumOptions || options || [];

  const formattedOptions = React.useMemo(
    () => optionsList.map((e) => ({ label: t(Digit.Utils.locale.getTransformedLocale(`${schemaCode}_${e?.label}`)), value: e.value })),
    [optionsList, schemaCode, data]
  );
  const selectedOption = formattedOptions?.filter((obj) => (multiple ? value?.includes(obj.value) : obj.value == value));
  if (isLoading) {
    return <Loader />;
  }
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
