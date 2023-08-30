import React, { Fragment, useState, useEffect } from "react";
import PropTypes from "prop-types";
import MultiSelectDropdown from "../atoms/MultiSelectDropdown";
import Dropdown from "../atoms/Dropdown";
import { Loader } from "../atoms/Loader";
import { useTranslation } from "react-i18next";

const LocationDropdownWrapper = ({ populators, formData, props, inputRef, errors, setValue, variant }) => {
  const [options, setOptions] = useState([]);

  const { t } = useTranslation();
  const tenantId = window?.Digit?.ULBService?.getCurrentTenantId();
  const headerLocale = window?.Digit?.Utils?.locale?.getTransformedLocale(tenantId);

  const { isLoading, data: wardsAndLocalities } = window?.Digit?.Hooks.useLocation(tenantId, "Ward", {
    select: (data) => {
      const wards = [];
      const localities = {};
      data?.TenantBoundary[0]?.boundary.forEach((item) => {
        localities[item?.code] = item?.children.map((item) => ({
          code: item.code,
          name: item.name,
          i18nKey: `${headerLocale}_ADMIN_${item?.code}`,
          label: item?.label,
        }));
        wards.push({ code: item.code, name: item.name, i18nKey: `${headerLocale}_ADMIN_${item?.code}` });
      });

      return {
        wards,
        localities,
      };
    },
  });

  useEffect(() => {
    if (wardsAndLocalities) {
      if (populators.type === "ward") {
        setOptions(wardsAndLocalities?.wards);
      } else {
        //here you need to set the localities based on the selected ward
        let locs = [];
        const selectedWardsCodes = formData?.ward?.map((row) => row.code);
        selectedWardsCodes?.forEach((code) => {
          locs = [...locs, ...wardsAndLocalities?.localities?.[code]];
        });
        setOptions(locs);
      }
    }
  }, [wardsAndLocalities, formData?.ward]);

  if (isLoading) {
    return <Loader />;
  }

  return (
    <>
      {populators.allowMultiSelect && (
        <div style={{ display: "grid", gridAutoFlow: "row" }}>
          <MultiSelectDropdown
            options={options}
            optionsKey={populators?.optionsKey}
            props={props}
            isPropsNeeded={true}
            onSelect={(e) => {
              if (populators.type === "ward") {
                setValue("locality", []);
              }
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
            variant={variant}
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
          value={props?.value?.[0]}
          select={(e) => {
            props.onChange([e], populators.name);
          }}
          selected={props?.value?.[0] || populators.defaultValue}
          defaultValue={props?.value?.[0] || populators.defaultValue}
          t={t}
          errorStyle={errors?.[populators.name]}
          optionCardStyles={populators?.optionsCustomStyle}
          variant={variant}
        />
      )}
    </>
  );
};

LocationDropdownWrapper.propTypes = {
  populators: PropTypes.shape({
    type: PropTypes.string.isRequired,
    allowMultiSelect: PropTypes.bool.isRequired,
    optionsKey: PropTypes.string,
    defaultText: PropTypes.string,
    selectedText: PropTypes.string,
    defaultValue: PropTypes.string,
    optionsCustomStyle: PropTypes.object,
  }).isRequired,
  formData: PropTypes.object.isRequired,
  props: PropTypes.shape({
    onChange: PropTypes.func.isRequired,
    value: PropTypes.oneOfType([PropTypes.arrayOf(PropTypes.string), PropTypes.string]),
  }).isRequired,
  inputRef: PropTypes.oneOfType([PropTypes.object, PropTypes.func]),
  errors: PropTypes.object,
  setValue: PropTypes.func,
};

LocationDropdownWrapper.defaultProps = {
  populators: {
    optionsKey: "options",
    defaultText: "Select an option",
    selectedText: "Selected",
    defaultValue: "",
    optionsCustomStyle: {},
  },
  inputRef: null,
  errors: null,
};

export default LocationDropdownWrapper;
