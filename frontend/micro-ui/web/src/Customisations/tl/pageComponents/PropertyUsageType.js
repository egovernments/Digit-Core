import {
  CardLabel, CardLabelError, CitizenInfoLabel, Dropdown, FormStep, LabelFieldPair, RadioButtons
} from "@egovernments/digit-ui-react-components";
import React, { useEffect, useState } from "react";
import { useLocation } from "react-router-dom";

var Digit = window.Digit || {};

const TLUsageType = ({ t, config, onSelect, userType, formData, formState, setError, clearErrors, onBlur }) => {
  const [usageCategoryMajor, setPropertyPurpose] = useState(
    formData?.usageCategoryMajor && formData?.usageCategoryMajor?.code === "NONRESIDENTIAL.OTHERS"
      ? { code: `${formData?.usageCategoryMajor?.code}`, i18nKey: `PROPERTYTAX_BILLING_SLAB_OTHERS` }
      : formData?.usageCategoryMajor
  );
  
  const tenantId = Digit.ULBService.getCurrentTenantId();
  const stateId = tenantId.split(".")[0];
  const { data: Menu = { }, isLoading: menuLoading } = Digit.Hooks.pt.usePropertyMDMS(stateId, "PropertyTax", "UsageCategory") || { };
  let usagecat = [];
  usagecat = Menu?.PropertyTax?.UsageCategory || [];
  let i;
  let menu = [];

  const { pathname } = useLocation();
  const presentInModifyApplication = pathname.includes("modify");

  function usageCategoryMajorMenu(usagecat) {
    if (userType === "employee") {
      const catMenu = usagecat
        ?.filter((e) => e?.code.split(".").length <= 2 && e.code !== "NONRESIDENTIAL")
        ?.map((item) => {
          const arr = item?.code.split(".");
          if (arr.length == 2) return { i18nKey: "PROPERTYTAX_BILLING_SLAB_" + arr[1], code: item?.code };
          else return { i18nKey: "PROPERTYTAX_BILLING_SLAB_" + item?.code, code: item?.code };
        });
      return catMenu;
    } else {
      for (i = 0; i < 10; i++) {
        if (
          Array.isArray(usagecat) &&
          usagecat.length > 0 &&
          usagecat[i].code.split(".")[0] == "NONRESIDENTIAL" &&
          usagecat[i].code.split(".").length == 2
        ) {
          menu.push({ i18nKey: "PROPERTYTAX_BILLING_SLAB_" + usagecat[i].code.split(".")[1], code: usagecat[i].code });
        }
      }
      return menu;
    }
  }

  useEffect(() => {
    if (!menuLoading && presentInModifyApplication && userType === "employee") {
      const original = formData?.originalData?.usageCategory;
      const selectedOption = usageCategoryMajorMenu(usagecat).filter((e) => e.code === original)[0];
      setPropertyPurpose(selectedOption);
    }
  }, [menuLoading]);

  const onSkip = () => onSelect();


  function selectPropertyPurpose(value) {
    setPropertyPurpose(value);
  }

  function goNext() {
    if (usageCategoryMajor?.i18nKey === "PROPERTYTAX_BILLING_SLAB_OTHERS") {
      usageCategoryMajor.i18nKey = "PROPERTYTAX_BILLING_SLAB_NONRESIDENTIAL";
      onSelect(config.key, usageCategoryMajor);
    } else {
      onSelect(config.key, usageCategoryMajor);
    }
  }

  useEffect(() => {
    if (userType === "employee") {
      if (!usageCategoryMajor) {
        setError(config.key, { type: "required", message: t(`CORE_COMMON_REQUIRED_ERRMSG`) });
      } else {
        clearErrors(config.key);
      }
      goNext();
    }
  }, [usageCategoryMajor]);

  if (userType === "employee") {
    return (
      <React.Fragment>
        <LabelFieldPair>
          <CardLabel className="card-label-smaller">{t("PT_ASSESMENT_INFO_USAGE_TYPE") + " *"}</CardLabel>
          <Dropdown
            className="form-field"
            selected={usageCategoryMajor}
            disable={usageCategoryMajorMenu(usagecat)?.length === 1}
            option={usageCategoryMajorMenu(usagecat)}
            select={(e) => {
              selectPropertyPurpose(e);
            }}
            optionKey="i18nKey"
            onBlur={onBlur}
            t={t}
          />
        </LabelFieldPair>
        {formState.touched[config.key] ? (
          <CardLabelError style={{ width: "70%", marginLeft: "30%", fontSize: "12px", marginTop: "-21px" }}>
            {formState.errors?.[config.key]?.message}
          </CardLabelError>
        ) : null}
      </React.Fragment>
    );
  }

  return (
    <React.Fragment>
      <FormStep t={t} config={config} onSelect={goNext} onSkip={onSkip} isDisabled={!usageCategoryMajor}>
        <div>
          <RadioButtons
            t={t}
            optionsKey="i18nKey"
            isMandatory={config.isMandatory}
            //options={menu}
            options={usageCategoryMajorMenu(usagecat) || { }}
            selectedOption={usageCategoryMajor}
            onSelect={selectPropertyPurpose}
          />
          <input type="text"></input>
          <button>to say this is different element</button>
        </div>
      </FormStep>
      {<CitizenInfoLabel info={t("CS_FILE_APPLICATION_INFO_LABEL")} text={t("PT_USAGE_TYPE_INFO_MSG", usageCategoryMajor)} />}
    </React.Fragment>
  );
};

export default TLUsageType;
