import React, { useEffect, useMemo, useState, Fragment, useCallback } from "react";
import { useForm, Controller } from "react-hook-form";
import BreakLine from "../atoms/BreakLine";
import Card from "../atoms/Card";
import CardLabel from "../atoms/CardLabel";
import CardText from "../atoms/CardText";
// import CardLabelError from "../atoms/CardLabelError";
import CardSubHeader from "../atoms/CardSubHeader";
import CardSectionHeader from "../atoms/CardSectionHeader";
import CardLabelDesc from "../atoms/CardLabelDesc";
import CardLabelError from "../atoms/CardLabelError";
import TextArea from "../atoms/TextArea";
import TextInput from "../atoms/TextInput";
import ActionBar from "../atoms/ActionBar";
import SubmitBar from "../atoms/SubmitBar";
import LabelFieldPair from "../atoms/LabelFieldPair";
import LinkButton from "../atoms/LinkButton";

import { useTranslation } from "react-i18next";
import MobileNumber from "../atoms/MobileNumber";
import _ from "lodash";
import CustomDropdown from "../molecules/CustomDropdown";
import MultiUploadWrapper from "../molecules/MultiUploadWrapper";
import HorizontalNav  from "../atoms/HorizontalNav"

const wrapperStyles = {
  // "display":"flex",
  // "flexDirection":"column",
  // "justifyContent":"center",
  // "padding":"2rem",
  // "margin":"1rem",
  // "width":"80%",
  // "backgroundColor":"#FAFAFA",
  // "border": "1px solid #D6D5D4"
  display: "flex",
  flexDirection: "column",
  alignItems: "flex-start",
  border: "solid",
  borderRadius: "5px",
  padding: "10px",
  paddingTop: "20px",
  marginTop: "10px",
  borderColor: "#f3f3f3",
  background: "#FAFAFA",
  marginBottom: "20px",
};

/**
 *  formcomposer used to render forms
 *
 * @author jagankumar-egov
 *
 * @example
 *
 * refer this implementation of sample file
 * frontend/micro-ui/web/micro-ui-internals/packages/modules/AttendenceMgmt/src/pages/citizen/Sample.js
 *
 */

export const FormComposer = (props) => {
  const {
    register,
    handleSubmit,
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
  } = useForm({
    defaultValues: props.defaultValues,
  });
  const { t } = useTranslation();
  const formData = watch();

  useEffect(() => {
    if (
      props?.appData &&
      Object.keys(props?.appData)?.length > 0 &&
      (!_.isEqual(props?.appData, formData) || !_.isEqual(props?.appData?.ConnectionHolderDetails?.[0], formData?.ConnectionHolderDetails?.[0]))
    ) {
      reset({ ...props?.appData });
    }
  }, [props?.appData, formData, props?.appData?.ConnectionHolderDetails]);

  useEffect(() => {
    props.getFormAccessors && props.getFormAccessors({ setValue, getValues });
  }, []);

  function onSubmit(data) {
    props.onSubmit(data);
  }

  function onSecondayActionClick(data) {
    props.onSecondayActionClick();
  }

  useEffect(() => {
    props.onFormValueChange && props.onFormValueChange(setValue, formData, formState, reset);
  }, [formData]);

  const fieldSelector = (type, populators, isMandatory, disable = false, component, config, sectionFormCategory) => {
    let disableFormValidation = false;
    // disable form validation if section category does not matches with the current category
    // this will avoid validation for the other categories other than the current category.
    // sectionFormCategory comes as part of section config and currentFormCategory is a state managed by the FormComposer consumer.
    if (sectionFormCategory && props?.currentFormCategory) {
      disableFormValidation = sectionFormCategory !== props?.currentFormCategory ? true : false;
    }
    const Component = typeof component === "string" ? Digit.ComponentRegistryService.getComponent(component) : component;
    switch (type) {
      case "date":
      case "text":
      case "number":
      case "password":
      case "time":
         // if (populators.defaultValue) setTimeout(setValue(populators?.name, populators.defaultValue));
        return (
          <div className="field-container">
            {populators?.componentInFront ? (
              <span className={`component-in-front ${disable && "disabled"}`}>{populators.componentInFront}</span>
            ) : null}
            <Controller
              defaultValue={formData?.[populators.name] || populators.defaultValue}
              render={({ onChange, ref, value }) => (
                <TextInput
                  value={formData?.[populators.name] || populators.defaultValue}
                  type={type}
                  name={populators.name}
                  onChange={onChange}
                  inputRef={ref}
                  errorStyle={errors?.[populators.name]}
                  max={populators.max}
                  disable={disable}
                  style={type === "date" ? { paddingRight: "3px" } : ""}
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

      case "textarea":
        // if (populators.defaultValue) setTimeout(setValue(populators?.name, populators.defaultValue));
        return (
          <Controller
            defaultValue={formData?.[populators.name]}
            render={({ onChange, ref, value }) => (
              <TextArea
                className="field"
                value={formData?.[populators.name]}
                type={type}
                name={populators.name}
                onChange={onChange}
                inputRef={ref}
                disable={disable}
                errorStyle={errors?.[populators.name]}
              />
            )}
            name={populators.name}
            rules={!disableFormValidation ? { required: isMandatory, ...populators.validation } : {}}
            control={control}
          />
        );
      case "mobileNumber":
        return (
          <Controller
            render={(props) => (
              <MobileNumber
                inputRef={props.ref}
                className="field fullWidth"
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
                onChange(numberOfFiles>0?filesData:[]);
              }
              return (
                <MultiUploadWrapper
                  t={t}
                  module="works"
                  tenantId={Digit.ULBService.getCurrentTenantId()}
                  getFormState={getFileStoreData}
                  showHintBelow={populators?.showHintBelow ? true : false}
                  setuploadedstate={value}
                  allowedFileTypesRegex={populators.allowedFileTypes}
                  allowedMaxSizeInMB={populators.allowedMaxSizeInMB}
                  hintText={populators.hintText}
                  maxFilesAllowed={populators.maxFilesAllowed}
                  extraStyleName={{ padding: "0.5rem" }}
                  customClass={populators?.customClass}
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
                props={props}
                setError={setError}
                clearErrors={clearErrors}
                formState={formState}
                onBlur={props.onBlur}
                control={control}
                getValues={getValues}
              />
            )}
            name={config.key}
            control={control}
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
      default:
        return populators?.dependency !== false ? populators : null;
    }
  };

  const getCombinedStyle = (placementinBox) => {
    switch (placementinBox) {
      case 0:
        return {
          border: "solid",
          borderRadius: "5px",
          padding: "10px",
          paddingTop: "20px",
          marginTop: "10px",
          borderColor: "#f3f3f3",
          background: "#FAFAFA",
          marginBottom: "20px",
        };
      case 1:
        return {
          border: "solid",
          borderRadius: "5px",
          padding: "10px",
          paddingTop: "20px",
          marginTop: "-30px",
          borderColor: "#f3f3f3",
          background: "#FAFAFA",
          borderTop: "0px",
          borderBottom: "0px",
        };
      case 2:
        return {
          border: "solid",
          borderRadius: "5px",
          padding: "10px",
          paddingTop: "20px",
          marginTop: "-30px",
          borderColor: "#f3f3f3",
          background: "#FAFAFA",
          marginBottom: "20px",
          borderTop: "0px",
        };
    }
  };

  const titleStyle = { color: "#505A5F", fontWeight: "700", fontSize: "16px" };

  const getCombinedComponent = (section) => {
    if (section.head && section.subHead) {
      return (
        <>
          <CardSectionHeader style={props?.sectionHeadStyle ? props?.sectionHeadStyle : { margin: "5px 0px" }} id={section.headId}>
            {t(section.head)}
          </CardSectionHeader>
          <CardSectionHeader style={titleStyle} id={`${section.headId}_DES`}>
            {t(section.subHead)}
          </CardSectionHeader>
        </>
      );
    } else if (section.head) {
      return (
        <>
          <CardSectionHeader style={props?.sectionHeadStyle ? props?.sectionHeadStyle : {}} id={section.headId}>
            {t(section.head)}
          </CardSectionHeader>
        </>
      );
    } else {
      return <div></div>;
    }
  };

  const formFields = useCallback(
    (section, index, array, sectionFormCategory) => (
      <React.Fragment key={index}>
        {section && getCombinedComponent(section)}
        {section.body.map((field, index) => {
          if (props.inline)
            return (
              <React.Fragment key={index}>
                <div style={field.isInsideBox ? getCombinedStyle(field?.placementinbox) : {}}>
                  {!field.withoutLabel && (
                    <CardLabel
                      style={{ color: field.isSectionText ? "#505A5F" : "", marginBottom: props.inline ? "8px" : "revert" }}
                      className={field?.disable ? "disabled" : ""}
                    >
                      {t(field.label)}
                      {field.isMandatory ? " * " : null}
                      {field.labelChildren && field.labelChildren}
                    </CardLabel>
                  )}
                  {errors && errors[field.populators?.name] && Object.keys(errors[field.populators?.name]).length ? (
                    <CardLabelError>{t(field.populators.error || errors[field.populators?.name]?.message)}</CardLabelError>
                  ) : null}
                  <div style={field.withoutLabel ? { width: "100%" } : {}} className="field">
                    {fieldSelector(field.type, field.populators, field.isMandatory, field?.disable, field?.component, field, sectionFormCategory)}
                    {field?.description && (
                      <CardLabel
                        style={{
                          marginTop: "-24px",
                          fontSize: "16px",
                          fontWeight: "bold",
                          color: "#505A5F",
                          ...field?.descriptionStyles,
                        }}
                      >
                        {t(field.description)}
                      </CardLabel>
                    )}
                  </div>
                </div>
              </React.Fragment>
            );
          return (
            <Fragment>
              <LabelFieldPair
                key={index}
                style={
                  props?.showWrapperContainers && !field.hideContainer
                    ? { ...wrapperStyles }
                    : {  border: "none", background: "white" }
                }
              >
                {!field.withoutLabel && (
                  <CardLabel
                    style={{
                      color: field.isSectionText ? "#505A5F" : "",
                      marginBottom: props.inline ? "8px" : "revert",
                      fontWeight: props.isDescriptionBold ? "600" : null,
                    }}
                  >
                    {t(field.label)}
                    {field.isMandatory ? " * " : null}
                  </CardLabel>
                )}
                <div style={field.withoutLabel ? { width: "100%", ...props?.fieldStyle } : { ...props?.fieldStyle }} className="field">
                  {fieldSelector(field.type, field.populators, field.isMandatory, field?.disable, field?.component, field, sectionFormCategory)}
                  {field?.description && <CardText style={{ fontSize: "14px", marginTop: "-24px" }}>{t(field?.description)}</CardText>}
                </div>
              </LabelFieldPair>
              {field?.populators?.name && errors && errors[field?.populators?.name] && Object.keys(errors[field?.populators?.name]).length ? (
                <CardLabelError style={{ width: "70%", marginLeft: "30%", fontSize: "12px", marginTop: "-21px" }}>
                  {t(field?.populators?.error)}
                </CardLabelError>
              ) : null}
            </Fragment>
          );
        })}
        {!props.noBreakLine && (array.length - 1 === index ? null : <BreakLine style={props?.breaklineStyle ? props?.breaklineStyle : {}} />)}
      </React.Fragment>
    ),
    [props.config, formData]
  );
  const formFieldsAll = useMemo(
    () =>
      props.config?.map((section, index, array) => {
        return (
          <React.Fragment key={index}>
            {section && getCombinedComponent(section)}
            {section.body.map((field, index) => {
              if (props.inline)
                return (
                  <React.Fragment key={index}>
                    <div style={field.isInsideBox ? getCombinedStyle(field?.placementinbox) : {}}>
                      {!field.withoutLabel && (
                        <CardLabel
                          style={{ color: field.isSectionText ? "#505A5F" : "", marginBottom: props.inline ? "8px" : "revert" }}
                          className={field?.disable ? "disabled" : ""}
                        >
                          {t(field.label)}
                          {field.isMandatory ? " * " : null}
                          {field.labelChildren && field.labelChildren}
                        </CardLabel>
                      )}
                      {errors && errors[field.populators?.name] && Object.keys(errors[field.populators?.name]).length ? (
                        <CardLabelError>{t(field.populators.error || errors[field.populators?.name]?.message)}</CardLabelError>
                      ) : null}
                      <div style={field.withoutLabel ? { width: "100%" } : {}} className="field">
                        {fieldSelector(field.type, field.populators, field.isMandatory, field?.disable, field?.component, field)}
                        {field?.description && (
                          <CardLabel
                            style={{
                              marginTop: "-24px",
                              fontSize: "16px",
                              fontWeight: "bold",
                              color: "#505A5F",
                              ...field?.descriptionStyles,
                            }}
                          >
                            {t(field.description)}
                          </CardLabel>
                        )}
                      </div>
                    </div>
                  </React.Fragment>
                );
              return (
                <Fragment>
                  <LabelFieldPair key={index}>
                    {!field.withoutLabel && (
                      <CardLabel
                        style={{ color: field.isSectionText ? "#505A5F" : "", marginBottom: props.inline ? "8px" : "revert", ...props.fieldStyle }}
                      >
                        {t(field.label)}
                        {field.isMandatory ? " * " : null}
                      </CardLabel>
                    )}
                    <div style={field.withoutLabel ? { width: "100%", ...props?.fieldStyle } : {}} className="field">
                      {fieldSelector(field.type, field.populators, field.isMandatory, field?.disable, field?.component, field)}
                      {field?.description && <CardText style={{ fontSize: "14px", marginTop: "-24px" }}>{t(field?.description)}</CardText>}
                    </div>
                  </LabelFieldPair>
                  {field?.populators?.name && errors && errors[field?.populators?.name] && Object.keys(errors[field?.populators?.name]).length ? (
                    <CardLabelError style={{ width: "70%", marginLeft: "30%", fontSize: "12px", marginTop: "-21px" }}>
                      {t(field?.populators?.error)}
                    </CardLabelError>
                  ) : null}
                </Fragment>
              );
            })}
            {!props.noBreakLine && (array.length - 1 === index ? null : <BreakLine style={props?.breaklineStyle ? props?.breaklineStyle : {}} />)}
          </React.Fragment>
        );
      }),
    [props.config, formData]
  );

  const getCardStyles = (shouldDisplay = true) => {
    let styles = props.cardStyle || {};
    if (props.noBoxShadow) styles = { ...styles, boxShadow: "none" };
    if(!shouldDisplay) styles = {...styles, display : "none"}
    return styles;
  };

  const isDisabled = props.isDisabled || false;
  const checkKeyDown = (e) => {
    const keyCode = e.keyCode ? e.keyCode : e.key ? e.key : e.which;
    if (keyCode === 13) {
      e.preventDefault();
    }
  };

  const [activeLink, setActiveLink] = useState(props.horizontalNavConfig?props.horizontalNavConfig?.[0].name:null);

  useEffect(()=>{
    setActiveLink(props.horizontalNavConfig?.[0].name);
  },[props.horizontalNavConfig]);
  
  const renderFormFields = (props, section, index, array, sectionFormCategory) => (
      <React.Fragment key={index}>
          {!props.childrenAtTheBottom && props.children}
          {props.heading && <CardSubHeader style={{ ...props.headingStyle }}> {props.heading} </CardSubHeader>}
          {props.description && <CardLabelDesc className={"repos"}> {props.description} </CardLabelDesc>}
          {props.text && <CardText>{props.text}</CardText>}
          {formFields(section, index, array, sectionFormCategory)}
          {props.childrenAtTheBottom && props.children}
          {props.submitInForm && (
            <SubmitBar label={t(props.label)} style={{ ...props?.buttonStyle }} submit="submit" disabled={isDisabled} className="w-full" />
          )}
          {props.secondaryActionLabel && (
          <div className="primary-label-btn" style={{ margin: "20px auto 0 auto" }} onClick={onSecondayActionClick}>
            {props.secondaryActionLabel}
          </div>)}
      </React.Fragment>  
  );

  return (
    <form onSubmit={handleSubmit(onSubmit)} onKeyDown={(e) => checkKeyDown(e)} id={props.formId} className={props.className}>
      {props?.showMultipleCardsWithoutNavs ? (
          props?.config?.map((section, index, array) => {
            return !section.navLink && (
              <Card style={getCardStyles()} noCardStyle={props.noCardStyle}>
                {renderFormFields(props, section, index, array)}
              </Card>
            )
          })
        ) :  (         
          <Card style={getCardStyles()} noCardStyle={props.noCardStyle}>
            {
              props?.config?.map((section, index, array) => {
                return !section.navLink && (
                    <>
                      {renderFormFields(props, section, index, array)}
                    </>
                )
              })
            }
          </Card>
          )
      }
      { props?.showNavs && props.horizontalNavConfig && (
           <HorizontalNav configNavItems={props.horizontalNavConfig?props.horizontalNavConfig:null} showNav={true} activeLink={activeLink} setActiveLink={setActiveLink}>
           {props?.showMultipleCardsInNavs ? (
             props?.config?.map((section, index, array) => {
               return section.navLink ? (
                 <Card style={section.navLink !== activeLink ? getCardStyles(false) : getCardStyles()} noCardStyle={props.noCardStyle}>
                    {renderFormFields(props, section, index, array, section?.sectionFormCategory)}
                 </Card>
               ) : null
             })
           ) :   
             ( 
                 <Card style={getCardStyles()} noCardStyle={props.noCardStyle}>
                   {
                     props?.config?.map((section, index, array) => {
                      return section.navLink ?  (
                         <>
                            <div style={section.navLink !== activeLink ? {display : "none"} : {}}>
                              {renderFormFields(props, section, index, array, section?.sectionFormCategory)}
                            </div>
                         </>
                       ) : null
                     })
                   }
                 </Card>
             )
           }
           </HorizontalNav>
        )
      }
      {!props.submitInForm && props.label && (
        <ActionBar>
          <SubmitBar label={t(props.label)} submit="submit" disabled={isDisabled} />
          {props.onSkip && props.showSkip && <LinkButton style={props?.skipStyle} label={t(`CS_SKIP_CONTINUE`)} onClick={props.onSkip} />}
        </ActionBar>
      )}
    </form>
  );
};
