import React from "react";
import { CheckBox, InputTextAmount, MobileNumber, MultiSelectDropdown, Paragraph, TextArea, TextInput } from "../atoms";
import { ApiDropdown, CustomDropdown, LocationDropdownWrapper, MultiUploadWrapper } from "../molecules";
import { useForm, Controller } from "react-hook-form";
import UploadFileComposer from "./UploadFileComposer";
// import { CustomDropdown } from "../atoms-groups";

const Fields = (
  t,
  errors,
  type,
  populators,
  isMandatory,
  disable = false,
  component,
  config,
  sectionFormCategory,
  formData,
  control,
  selectedFormCategory,
  setValue,
  register,
  setError,
  clearErrors,
  formState,
  getValues,
  handleSubmit,
  reset,
  watch,
  trigger,
  unregister
) => {
  let disableFormValidation = false;
  // disable form validation if section category does not matches with the current category
  // this will avoid validation for the other categories other than the current category.
  // sectionFormCategory comes as part of section config and selectedFormCategory is a state managed by the FormComposer consumer.
  if (sectionFormCategory && selectedFormCategory) {
    disableFormValidation = sectionFormCategory !== selectedFormCategory ? true : false;
  }
  const Component = typeof component === "string" ? Digit.ComponentRegistryService.getComponent(component) : component;
  const customValidation = config?.populators?.validation?.customValidation;
  const customRules = customValidation ? { validate: customValidation } : {};
  const customProps = config?.customProps;
  switch (type) {
    case "date":
    case "text":
    case "number":
    case "password":
    case "time":
      return (
        <div className="field-container">
          {populators?.componentInFront ? <span className={`component-in-front ${disable && "disabled"}`}>{populators.componentInFront}</span> : null}
          <Controller
            defaultValue={formData?.[populators.name]}
            render={({ onChange, ref, value }) => (
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
              />
            )}
            name={populators.name}
            rules={!disableFormValidation ? { required: isMandatory, ...populators.validation, ...customRules } : {}}
            control={control}
          />
        </div>
      );
    case "amount":
      return (
        <div className="field-container">
          {populators?.componentInFront ? <span className={`component-in-front ${disable && "disabled"}`}>{populators.componentInFront}</span> : null}
          <Controller
            defaultValue={formData?.[populators.name]}
            render={({ onChange, ref, value }) => (
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
              />
            )}
            name={populators.name}
            rules={!disableFormValidation ? { required: isMandatory, ...populators.validation, ...customRules } : {}}
            control={control}
          />
        </div>
      );
    case "textarea":
      return (
        <div className="field-container">
          <Controller
            defaultValue={formData?.[populators.name]}
            render={({ onChange, ref, value }) => (
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
              />
            )}
            name={populators.name}
            rules={!disableFormValidation ? { required: isMandatory, ...populators.validation } : {}}
            control={control}
          />
        </div>
      );
    case "paragraph":
      return (
        <div className="field-container">
          <Controller
            defaultValue={formData?.[populators.name]}
            render={({ onChange, ref, value }) => (
              <Paragraph
                value={formData?.[populators.name]}
                name={populators.name}
                inputRef={ref}
                customClass={populators?.customClass}
                customStyle={populators?.customStyle}
              />
            )}
            name={populators.name}
            rules={!disableFormValidation ? { required: isMandatory, ...populators.validation } : {}}
            control={control}
          />
        </div>
      );
    case "mobileNumber":
      return (
        <div className="field-container">
          <Controller
            render={(props) => (
              <MobileNumber
                inputRef={props.ref}
                // className="field fullWidth"
                onChange={props.onChange}
                value={props.value}
                disable={disable}
                {...props}
                errorStyle={errors?.[populators.name]}
              />
            )}
            defaultValue={populators.defaultValue}
            name={populators?.name}
            rules={!disableFormValidation ? { required: isMandatory, ...populators.validation } : {}}
            control={control}
          />
        </div>
      );
    case "custom":
      return (
        <Controller
          render={(props) => populators.component({ ...props, setValue }, populators.customProps)}
          defaultValue={populators.defaultValue}
          name={populators?.name}
          control={control}
        />
      );
    case "checkbox":
      return (
        <Controller
          name={`${populators.name}`}
          control={control}
          defaultValue={formData?.[populators.name]}
          rules={{ required: populators?.isMandatory }}
          render={(props) => {
            return (
              <div style={{ display: "grid", gridAutoFlow: "row" }}>
                <CheckBox
                  onChange={(e) => {
                    // const obj = {
                    //   ...props.value,
                    //   [e.target.value]: e.target.checked
                    // }

                    props.onChange(e.target.checked);
                  }}
                  value={formData?.[populators.name]}
                  checked={formData?.[populators.name]}
                  label={t(`${populators?.title}`)}
                  styles={populators?.styles}
                  style={populators?.labelStyles}
                  customLabelMarkup={populators?.customLabelMarkup}
                />
              </div>
            );
          }}
        />
      );
    case "multiupload":
      return (
        <Controller
          name={`${populators.name}`}
          control={control}
          rules={!disableFormValidation ? { required: false } : {}}
          render={({ onChange, ref, value = [] }) => {
            function getFileStoreData(filesData) {
              const numberOfFiles = filesData.length;
              let finalDocumentData = [];
              if (numberOfFiles > 0) {
                filesData.forEach((value) => {
                  finalDocumentData.push({
                    fileName: value?.[0],
                    fileStoreId: value?.[1]?.fileStoreId?.fileStoreId,
                    documentType: value?.[1]?.file?.type,
                  });
                });
              }
              //here we need to update the form the same way as the state of the reducer in multiupload, since Upload component within the multiupload wrapper uses that same format of state so we need to set the form data as well in the same way. Previously we were altering it and updating the formData
              onChange(numberOfFiles > 0 ? filesData : []);
            }
            return (
              <MultiUploadWrapper
                t={t}
                module="works"
                tenantId={Digit.ULBService.getCurrentTenantId()}
                getFormState={getFileStoreData}
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
              />
            );
          }}
        />
      );
    case "select":
    case "radio":
    case "dropdown":
    case "radioordropdown":
      return (
        <div className="field-container">
          <Controller
            render={(props) => (
              <CustomDropdown
                t={t}
                label={config?.label}
                type={type}
                onBlur={props.onBlur}
                value={props.value}
                inputRef={props.ref}
                onChange={props.onChange}
                config={populators}
                disable={config?.disable}
                errorStyle={errors?.[populators.name]}
              />
            )}
            rules={!disableFormValidation ? { required: isMandatory, ...populators.validation } : {}}
            defaultValue={formData?.[populators.name]}
            name={config.key}
            control={control}
          />
        </div>
      );
    case "component":
      return (
        <Controller
          render={(props) => (
            <Component
              userType={"employee"}
              t={t}
              setValue={setValue}
              onSelect={setValue}
              config={config}
              data={formData}
              formData={formData}
              register={register}
              errors={errors}
              props={{ ...props, ...customProps }}
              setError={setError}
              clearErrors={clearErrors}
              formState={formState}
              onBlur={props.onBlur}
              control={control}
              sectionFormCategory={sectionFormCategory}
              selectedFormCategory={selectedFormCategory}
              getValues={getValues}
              watch={watch}
              unregister={unregister}
            />
          )}
          name={config.key}
          control={control}
        />
      );
    case "documentUpload":
      return (
        <UploadFileComposer
          module={config?.module}
          config={config}
          Controller={Controller}
          register={register}
          formData={formData}
          errors={errors}
          control={control}
          customClass={config?.customClass}
          customErrorMsg={config?.error}
          localePrefix={config?.localePrefix}
        />
      );
    case "form":
      return (
        <form>
          <Component
            userType={"employee"}
            t={t}
            setValue={setValue}
            onSelect={setValue}
            config={config}
            data={formData}
            formData={formData}
            register={register}
            errors={errors}
            setError={setError}
            clearErrors={clearErrors}
            formState={formState}
            control={control}
          />
        </form>
      );
    case "locationdropdown":
      return (
        <div className="field-container">
          <Controller
            name={`${populators.name}`}
            control={control}
            defaultValue={formData?.[populators.name]}
            rules={{ required: populators?.isMandatory, ...populators.validation }}
            render={(props) => {
              return (
                // <div style={{ display: "grid", gridAutoFlow: "row" }}>
                  <LocationDropdownWrapper
                    props={props}
                    populators={populators}
                    formData={formData}
                    inputRef={props.ref}
                    errors={errors}
                    setValue={setValue}
                  />
                // </div>
              );
            }}
          />
        </div>
      );
    case "apidropdown":
      return (
        <Controller
          name={`${populators.name}`}
          control={control}
          defaultValue={formData?.[populators.name]}
          rules={{ required: populators?.isMandatory, ...populators.validation }}
          render={(props) => {
            return (
              <div style={{ display: "grid", gridAutoFlow: "row" }}>
                <ApiDropdown props={props} populators={populators} formData={formData} inputRef={props.ref} errors={errors} />
              </div>
            );
          }}
        />
      );
    case "multiselectdropdown":
      return (
        <Controller
          name={`${populators.name}`}
          control={control}
          defaultValue={formData?.[populators.name]}
          rules={{ required: isMandatory }}
          render={(props) => {
            return (
              <div style={{ display: "grid", gridAutoFlow: "row" }}>
                <MultiSelectDropdown
                  options={populators?.options}
                  optionsKey={populators?.optionsKey}
                  props={props}
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
                  selected={props?.value || []}
                  defaultLabel={t(populators?.defaultText)}
                  defaultUnit={t(populators?.selectedText)}
                  config={populators}
                />
              </div>
            );
          }}
        />
      );
    default:
      // return populators?.dependency !== false ? populators : null;
      return null;
  }
};

export default Fields;
