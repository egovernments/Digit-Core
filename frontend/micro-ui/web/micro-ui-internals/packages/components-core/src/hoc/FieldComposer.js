import React, { Fragment } from "react";
import {
  CardText,
  CheckBox,
  ErrorMessage,
  Header,
  InputTextAmount,
  MobileNumber,
  MultiSelectDropdown,
  Paragraph,
  TextArea,
  TextInput,
} from "../atoms";
import { ApiDropdown, CustomDropdown, LocationDropdownWrapper, MultiUploadWrapper } from "../molecules";
import UploadFileComposer from "./UploadFileComposer";
import { useTranslation } from "react-i18next";

// const FieldComposer = (type, populators, isMandatory, disable = false, component, config, sectionFormCategory, formData, selectedFormCategory) => {
const FieldComposer = ({
  type,
  populators,
  isMandatory,
  disable = false,
  component,
  config,
  sectionFormCategory,
  formData,
  selectedFormCategory,
  onChange,
  ref,
  value,
  props,
  errors,
  onBlur,
  controllerProps,
  variant,
}) => {
  const { t } = useTranslation();
  let disableFormValidation = false;
  if (sectionFormCategory && selectedFormCategory) {
    disableFormValidation = sectionFormCategory !== selectedFormCategory ? true : false;
  }
  const Component = typeof component === "string" ? Digit.ComponentRegistryService.getComponent(component) : component;
  const customValidation = config?.populators?.validation?.customValidation;
  const customRules = customValidation ? { validate: customValidation } : {};
  const customProps = config?.customProps;
  const renderField = () => {
    switch (type) {
      case "date":
      case "text":
      case "number":
      case "password":
      case "time":
        return (
          <TextInput
            value={formData?.[populators.name]}
            type={type}
            name={populators.name}
            onChange={onChange}
            inputRef={ref}
            errorStyle={errors?.[populators.name]}
            max={populators?.validation?.max}
            min={populators?.validation?.min}
            disable={disable}
            style={type === "date" ? { paddingRight: "3px" } : ""}
            maxlength={populators?.validation?.maxlength}
            minlength={populators?.validation?.minlength}
            customIcon={populators?.customIcon}
            customClass={populators?.customClass}
            variant={variant ? variant : errors?.[populators.name] ? "digit-field-error" : ""}
          />
        );
      case "amount":
        return (
          <InputTextAmount
            value={formData?.[populators.name]}
            type={"text"}
            name={populators.name}
            onChange={onChange}
            inputRef={ref}
            errorStyle={errors?.[populators.name]}
            max={populators?.validation?.max}
            min={populators?.validation?.min}
            disable={disable}
            style={type === "date" ? { paddingRight: "3px" } : ""}
            maxlength={populators?.validation?.maxlength}
            minlength={populators?.validation?.minlength}
            customIcon={populators?.customIcon}
            customClass={populators?.customClass}
            prefix={populators?.prefix}
            intlConfig={populators?.intlConfig}
            variant={variant ? variant : errors?.[populators.name] ? "digit-field-error" : ""}
          />
        );
      case "textarea":
        return (
          <div className="digit-field-container">
            <TextArea
              value={formData?.[populators.name]}
              type={type}
              name={populators.name}
              onChange={onChange}
              inputRef={ref}
              disable={disable}
              errorStyle={errors?.[populators.name]}
              style={{ marginTop: 0 }}
              maxlength={populators?.validation?.maxlength}
              minlength={populators?.validation?.minlength}
              variant={variant ? variant : errors?.[populators.name] ? "digit-field-error" : ""}
            />
          </div>
        );
      case "paragraph":
        return (
          <div className="digit-field-container">
            <Paragraph
              value={formData?.[populators.name]}
              name={populators.name}
              inputRef={ref}
              customClass={populators?.customClass}
              customStyle={populators?.customStyle}
              variant={variant ? variant : errors?.[populators.name] ? "digit-field-error" : ""}
            />
          </div>
        );
      case "mobileNumber":
        return (
          <div className="digit-field-container">
            <MobileNumber
              inputRef={ref}
              onChange={onChange}
              value={value}
              disable={disable}
              // {...props}
              errorStyle={errors?.[populators.name]}
              variant={variant ? variant : errors?.[populators.name] ? "digit-field-error" : ""}
            />
          </div>
        );
      case "custom":
        return populators.component;
      case "checkbox":
        return (
          <div style={{ display: "grid", gridAutoFlow: "row" }}>
            <CheckBox
              onChange={(e) => {
                // const obj = {
                //   ...props.value,
                //   [e.target.value]: e.target.checked
                // }
                onChange(e.target.checked);
              }}
              value={formData?.[populators.name]}
              checked={formData?.[populators.name]}
              label={t(`${populators?.title}`)}
              styles={populators?.styles}
              style={populators?.labelStyles}
              customLabelMarkup={populators?.customLabelMarkup}
              variant={variant ? variant : errors?.[populators.name] ? "digit-field-error" : ""}
            />
          </div>
        );
      case "multiupload":
        return (
          <MultiUploadWrapper
            t={t}
            module="works"
            tenantId={Digit.ULBService.getCurrentTenantId()}
            // getFormState={getFileStoreData}              // TODO: need to discuss and should be add later
            showHintBelow={populators?.showHintBelow ? true : false}
            setuploadedstate={value || []}
            allowedFileTypesRegex={populators.allowedFileTypes}
            allowedMaxSizeInMB={populators.allowedMaxSizeInMB}
            hintText={populators.hintText}
            maxFilesAllowed={populators.maxFilesAllowed}
            extraStyleName={{ padding: "0.5rem" }}
            customClass={populators?.customClass}
            customErrorMsg={populators?.errorMessage}
            containerStyles={{ ...populators?.containerStyles }}
            variant={variant ? variant : errors?.[populators.name] ? "digit-field-error" : ""}
          />
        );
      case "select":
      case "radio":
      case "dropdown":
      case "radioordropdown":
        return (
          <div className="digit-field-container">
            <CustomDropdown
              t={t}
              label={config?.label}
              type={type}
              onBlur={onBlur}
              value={value}
              inputRef={ref}
              onChange={onChange}
              config={populators}
              disable={config?.disable}
              errorStyle={errors?.[populators.name]}
              variant={variant ? variant : errors?.[populators.name] ? "digit-field-error" : ""}
            />
          </div>
        );
      case "component":
        return (
          <Component
            userType={"employee"}
            t={t}
            setValue={controllerProps?.setValue}
            onSelect={controllerProps?.setValue}
            config={config}
            data={formData}
            formData={formData}
            register={controllerProps?.register}
            errors={errors}
            props={{ ...props, ...customProps }}
            setError={controllerProps?.setError}
            clearErrors={controllerProps?.clearErrors}
            formState={controllerProps?.formState}
            onBlur={onBlur}
            control={controllerProps?.control}
            sectionFormCategory={sectionFormCategory}
            selectedFormCategory={selectedFormCategory}
            getValues={controllerProps?.getValues}
            watch={controllerProps?.watch}
            unregister={controllerProps?.unregister}
          />
        );
      case "documentUpload":
        return (
          <UploadFileComposer
            module={config?.module}
            config={config}
            // Controller={Controller}        // TODO: NEED TO DISCUSS ON THIS
            register={controllerProps?.register}
            formData={formData}
            errors={errors}
            control={controllerProps?.control}
            customClass={config?.customClass}
            customErrorMsg={config?.error}
            localePrefix={config?.localePrefix}
            variant={variant ? variant : errors?.[populators.name] ? "digit-field-error" : ""}
          />
        );
      case "form":
        return (
          <form>
            <Component
              userType={"employee"}
              t={t}
              setValue={controllerProps?.setValue}
              onSelect={controllerProps?.setValue}
              config={config}
              data={formData}
              formData={formData}
              register={controllerProps?.register}
              errors={errors}
              setError={controllerProps?.setError}
              clearErrors={controllerProps?.clearErrors}
              formState={controllerProps?.formState}
              control={controllerProps?.control}
              variant={variant ? variant : errors?.[populators.name] ? "digit-field-error" : ""}
            />
          </form>
        );
      case "locationdropdown":
        return (
          <div className="digit-field-container">
            <LocationDropdownWrapper
              props={{ config, onChange, value }}
              populators={populators}
              formData={formData}
              inputRef={ref}
              errors={errors}
              setValue={controllerProps?.setValue}
              variant={variant ? variant : errors?.[populators.name] ? "digit-field-error" : ""}
            />
          </div>
        );
      case "apidropdown":
        return (
          <div style={{ display: "grid", gridAutoFlow: "row" }}>
            <ApiDropdown
              props={props}
              populators={populators}
              formData={formData}
              inputRef={ref}
              errors={errors}
              variant={variant ? variant : errors?.[populators.name] ? "digit-field-error" : ""}
            />
          </div>
        );
      case "multiselectdropdown":
        return (
          <div style={{ display: "grid", gridAutoFlow: "row" }}>
            <MultiSelectDropdown
              options={populators?.options}
              optionsKey={populators?.optionsKey}
              props={props}
              isPropsNeeded={true}
              onSelect={(e) => {
                onChange(
                  e
                    ?.map((row) => {
                      return row?.[1] ? row[1] : null;
                    })
                    .filter((e) => e)
                );
              }}
              selected={value || []}
              defaultLabel={t(populators?.defaultText)}
              defaultUnit={t(populators?.selectedText)}
              config={populators}
              variant={variant ? variant : errors?.[populators.name] ? "digit-field-error" : ""}
            />
          </div>
        );
      default:
        return null;
    }
  };

  return (
    <>
      {!config.withoutLabel && (
        <Header
          style={{
            color: config?.isSectionText ? "#505A5F" : "",
            marginBottom: props?.inline ? "8px" : "revert",
            fontWeight: props?.isDescriptionBold ? "600" : null,
          }}
          className="label"
        >
          {t(config.label)}
          {config?.appendColon ? " : " : null}
          {config.isMandatory ? " * " : null}
        </Header>
      )}
      <div style={config.withoutLabel ? { width: "100%", ...props?.fieldStyle } : { ...props?.fieldStyle }} className="digit-field">
        {renderField()}
        {config?.description && <CardText style={{ fontSize: "14px", marginTop: "-24px" }}>{t(config?.description)}</CardText>}
        {populators?.name && errors && errors[populators?.name] && Object.keys(errors[populators?.name]).length ? (
          <ErrorMessage message={t(populators?.error)} />
        ) : // {t(field?.populators?.error)}
        // </ErrorMessage>
        null}
      </div>
    </>
  );
};

export default FieldComposer;
