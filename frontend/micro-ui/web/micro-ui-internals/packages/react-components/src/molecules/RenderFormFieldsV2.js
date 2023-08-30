import React from "react";
import { useTranslation } from "react-i18next";
import { Controller } from "react-hook-form";
import CardLabelError from "../atoms/CardLabelError";
import LabelFieldPair from "../atoms/LabelFieldPair";
import CardLabel from "../atoms/CardLabel";
import TextInput from "../atoms/TextInput";
import TextArea from "../atoms/TextArea";
import CustomDropdown from "./CustomDropdown";
import MobileNumber from "../atoms/MobileNumber";
import DateRangeNew from "./DateRangeNew";
import MultiUploadWrapper from "./MultiUploadWrapper";
import MultiSelectDropdown from "../atoms/MultiSelectDropdown";
import LocationDropdownWrapper from "./LocationDropdownWrapper";
import WorkflowStatusFilter from "./WorkflowStatusFilter";
import ApiDropdown from "./ApiDropdown";
import FieldController from "@egovernments/digit-ui-components-core/src/hoc/FieldController";
import FieldComposer from "@egovernments/digit-ui-components-core/src/hoc/FieldComposer";
const RenderFormFieldsV2 = ({ data, ...props }) => {
  const { t } = useTranslation();
  const {
    fields,
    control,
    formData,
    errors,
    register,
    setValue,
    getValues,
    setError,
    clearErrors,
    apiDetails,
    reset,
    watch,
    trigger,
    formState,
    unregister,
  } = props;
  
  const fieldSelector = (type, populators, isMandatory, disable = false, component, config) =>
    // Calling field controller to render all label and fields
    FieldController({
      type: type,
      populators: populators,
      isMandatory: isMandatory,
      disable: disable,
      component: component,
      config: config,
      formData: formData,
      control: control,
      props: props,
      errors: errors,
      controllerProps: {
        register,
        setValue,
        getValues,
        reset,
        watch,
        trigger,
        control,
        formState,
        errors,
        setError,
        clearErrors,
        unregister,
      },
    });

  return (
    <React.Fragment>
      {fields?.map((item, index) => {
        return (
          <LabelFieldPair key={index}>
            {item.label && (
              <CardLabel style={{ ...props.labelStyle, marginBottom: "0.4rem" }}>
                {t(item.label)}
                {item?.isMandatory ? " * " : null}
              </CardLabel>
            )}

            {fieldSelector(item.type, item.populators, item.isMandatory, item?.disable, item?.component, item)}

            {item?.populators?.name && errors && errors[item?.populators?.name] && Object.keys(errors[item?.populators?.name]).length ? (
              <CardLabelError style={{ fontSize: "12px", marginTop: "-20px" }}>{t(item?.populators?.error)}</CardLabelError>
            ) : null}
          </LabelFieldPair>
        );
      })}
    </React.Fragment>
  );
};

export default RenderFormFieldsV2;
