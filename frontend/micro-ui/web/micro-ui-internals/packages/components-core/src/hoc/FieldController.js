import React from "react";
import FieldComposer from "./FieldComposer";
import { Controller } from "react-hook-form";

function FieldController(
  type,
  populators,
  isMandatory,
  disable,
  component,
  config,
  sectionFormCategory,
  formData,
  selectedFormCategory,
  control,
  props,
  errors,
  controllerProps
) {
  let disableFormValidation = false;
  if (sectionFormCategory && selectedFormCategory) {
    disableFormValidation = sectionFormCategory !== selectedFormCategory ? true : false;
  }
  const customValidation = config?.populators?.validation?.customValidation;
  const customRules = customValidation ? { validate: customValidation } : {};
  const customProps = config?.customProps;
  return (
    <Controller
      defaultValue={formData?.[populators.name]}
      render={({ onChange, ref, value, onBlur }) => (
        <FieldComposer
          type={type}
          populators={populators}
          isMandatory={isMandatory}
          disable={disable}
          component={component}
          config={config}
          sectionFormCategory={sectionFormCategory}
          formData={formData}
          selectedFormCategory={selectedFormCategory}
          onChange={onChange}
          ref={ref}
          value={value}
          props={props}
          errors={errors}
          onBlur={onBlur}
          controllerProps={controllerProps}
        />
      )}
      name={populators.name}
      rules={!disableFormValidation ? { required: isMandatory, ...populators.validation, ...customRules } : {}}
      control={control}
    />
  );
}

export default FieldController;
