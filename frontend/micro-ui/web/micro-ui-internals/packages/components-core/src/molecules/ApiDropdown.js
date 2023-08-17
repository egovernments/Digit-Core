import React, { Fragment, useState, useEffect } from "react";
import PropTypes from "prop-types";
import MultiSelectDropdown from "../atoms/MultiSelectDropdown";
import Dropdown from "../atoms/Dropdown";
import { Loader } from "../atoms/Loader";
import { useTranslation } from "react-i18next";
import _ from "lodash";

const ApiDropdown = ({ populators, formData, props, inputRef, errors }) => {
  const [options, setOptions] = useState([]);

  const { t } = useTranslation();

  const reqCriteria = Digit?.Customizations?.[populators?.masterName]?.[populators?.moduleName]?.[populators?.customfn]();

  const { isLoading: isApiLoading, data: apiData, revalidate, isFetching: isApiFetching } = window?.Digit?.Hooks.useCustomAPIHook(reqCriteria);

  useEffect(() => {
    setOptions(apiData);
  }, [apiData]);

  if (isApiLoading) return <Loader />;

  return (
    <>
      {populators.allowMultiSelect && (
        <div style={{ display: "grid", gridAutoFlow: "row" }}>
          <MultiSelectDropdown
            options={options}
            optionsKey={populators?.optionsKey}
            props={props} //these are props from Controller
            isPropsNeeded={true}
            onSelect={(e) => {
              props.onChange(
                e
                  ?.map((row) => {
                    return row?.[1] ? row[1] : null;
                  })
                  .filter((e) => e)
              );
            }}
            selected={props?.value}
            defaultLabel={t(populators?.defaultText)}
            defaultUnit={t(populators?.selectedText)}
            config={populators}
          />
        </div>
      )}
      {!populators.allowMultiSelect && (
        <Dropdown
          inputRef={inputRef}
          style={{ display: "flex", justifyContent: "space-between" }}
          option={options}
          key={populators.name}
          optionKey={populators?.optionsKey}
          value={props.value?.[0]}
          select={(e) => {
            props.onChange([e], populators.name);
          }}
          selected={props.value?.[0] || populators.defaultValue}
          defaultValue={props.value?.[0] || populators.defaultValue}
          t={t}
          errorStyle={errors?.[populators.name]}
          optionCardStyles={populators?.optionsCustomStyle}
        />
      )}
    </>
  );
};

ApiDropdown.propTypes = {
  populators: PropTypes.shape({
    allowMultiSelect: PropTypes.bool.isRequired,
    masterName: PropTypes.string,
    moduleName: PropTypes.string,
    customfn: PropTypes.string,
    optionsKey: PropTypes.string,
    defaultText: PropTypes.string,
    selectedText: PropTypes.string,
    name: PropTypes.string,
    defaultValue: PropTypes.string,
    optionsCustomStyle: PropTypes.object,
  }).isRequired,
  formData: PropTypes.object,
  props: PropTypes.shape({
    isApiLoading: PropTypes.bool,
    options: PropTypes.array,
    className: PropTypes.string,
    style: PropTypes.object,
    value: PropTypes.oneOfType([PropTypes.string, PropTypes.array]),
    onChange: PropTypes.func,
  }).isRequired,
  inputRef: PropTypes.object,
  errors: PropTypes.object,
};

export default ApiDropdown;
