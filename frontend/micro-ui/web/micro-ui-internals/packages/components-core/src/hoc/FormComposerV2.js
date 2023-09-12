import React, { useEffect, useMemo, useState, Fragment, useCallback } from "react";
import { useForm, Controller } from "react-hook-form";
import { useTranslation } from "react-i18next";
import _ from "lodash";

// atoms need for initial setup
import BreakLine from "../atoms/BreakLine";
import Card from "../atoms/Card";
import Header from "../atoms/Header";
import Button from "../atoms/Button";
import ActionLinks from "../atoms/ActionLinks";
import ActionBar from "../atoms/ActionBar";
import LabelFieldPair from "../atoms/LabelFieldPair";
import ErrorMessage from "../atoms/ErrorMessage";
import HorizontalNav from "../atoms/HorizontalNav";
import { CardText, Toast } from "../atoms";

// import Fields from "./Fields";    //This is a field selector pickup from formcomposer
import FieldController from "./FieldController";

const wrapperStyles = {
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
  const selectedFormCategory = props?.currentFormCategory;
  const [showErrorToast, setShowErrorToast] = useState(false);
  //clear all errors if user has changed the form category.
  //This is done in case user first click on submit and have errors in cat 1, switches to cat 2 and hit submit with errors
  //So, he should not get error prompts from previous cat 1 on cat 2 submit.
  useEffect(() => {
    clearErrors();
  }, [selectedFormCategory]);

  useEffect(() => {
    if (Object.keys(formState?.errors).length > 0 && formState?.submitCount > 0) {
      setShowErrorToast(true);
    }
  }, [formState?.errors, formState?.submitCount]);

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
    props.onFormValueChange && props.onFormValueChange(setValue, formData, formState, reset, setError, clearErrors, trigger, getValues);
  }, [formData]);

  const fieldSelector = (type, populators, isMandatory, disable = false, component, config, sectionFormCategory) =>
    // Calling field controller to render all label and fields
    FieldController({
      type: type,
      populators: populators,
      isMandatory: isMandatory,
      disable: disable,
      component: component,
      config: config,
      sectionFormCategory: sectionFormCategory,
      formData: formData,
      selectedFormCategory: selectedFormCategory,
      control: control,
      props: props,
      errors: errors,
      controllerProps: {
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
      },
    });

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
          <Header
            className={`digit-card-section-header`}
            style={props?.sectionHeadStyle ? props?.sectionHeadStyle : { margin: "5px 0px" }}
            id={section.headId}
          >
            {t(section.head)}
          </Header>
          <Header style={titleStyle} id={`${section.headId}_DES`}>
            {t(section.subHead)}
          </Header>
        </>
      );
    } else if (section.head) {
      return (
        <>
          <Header className={`digit-card-section-header`} style={props?.sectionHeadStyle ? props?.sectionHeadStyle : {}} id={section.headId}>
            {t(section.head)}
          </Header>
        </>
      );
    } else {
      return <div></div>;
    }
  };

  const closeToast = () => {
    setShowErrorToast(false);
  };

  //remove Toast from 3s
  useEffect(() => {
    if (showErrorToast) {
      setTimeout(() => {
        closeToast();
      }, 3000);
    }
  }, [showErrorToast]);

  const formFields = useCallback(
    (section, index, array, sectionFormCategory) => (
      <React.Fragment key={index}>
        {section && getCombinedComponent(section)}
        {section.body.map((field, index) => {
          if (field?.populators?.hideInForm) return null;
          if (props.inline)
            return (
              <React.Fragment key={index}>
                <div style={field.isInsideBox ? getCombinedStyle(field?.placementinbox) : field.inline ? { display: "flex" } : {}}>
                  {/* {!field.withoutLabel && (
                    <Header
                      style={{ color: field.isSectionText ? "#505A5F" : "", marginBottom: props.inline ? "8px" : "revert" }}
                      className={` ${field?.disable ? `disabled ${props?.labelBold ? "bolder" : ""}` : `${props?.labelBold ? "bolder" : ""}`}`}
                    >
                      {t(field.label)}
                      {field.isMandatory ? " * " : null}
                      {field.labelChildren && field.labelChildren}
                    </Header>
                  )} */}
                  {/* {errors && errors[field.populators?.name] && Object.keys(errors[field.populators?.name]).length ? (
                    <ErrorMessage>{t(field.populators.error || errors[field.populators?.name]?.message)}</ErrorMessage>
                  ) : null} */}
                  <div style={field.withoutLabel ? { width: "100%" } : {}} className="digit-field">
                    {fieldSelector(field.type, field.populators, field.isMandatory, field?.disable, field?.component, field, sectionFormCategory)}
                    {field?.description && (
                      <Header
                        style={{
                          marginTop: "-24px",
                          fontSize: "16px",
                          fontWeight: "bold",
                          color: "#505A5F",
                          ...field?.descriptionStyles,
                        }}
                        className="bolder"
                      >
                        {t(field.description)}
                      </Header>
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
                    ? { ...wrapperStyles, ...field?.populators?.customStyle }
                    : { border: "none", background: "white", ...field?.populators?.customStyle }
                }
              >
                {fieldSelector(field.type, field.populators, field.isMandatory, field?.disable, field?.component, field, sectionFormCategory)}

                {/* Commenting to initialize & check Field Controller and composer which render label and field Should remove later*/}
                {/*{!field.withoutLabel && (
                  <Header
                    style={{
                      color: field.isSectionText ? "#505A5F" : "",
                      marginBottom: props.inline ? "8px" : "revert",
                      fontWeight: props.isDescriptionBold ? "600" : null,
                    }}
                    className="label"
                  >
                    {t(field.label)}
                    {field?.appendColon ? " : " : null}
                    {field.isMandatory ? " * " : null}
                  </Header>
                )}
                <div style={field.withoutLabel ? { width: "100%", ...props?.fieldStyle } : { ...props?.fieldStyle }} className="digit-field">
                  {fieldSelector(field.type, field.populators, field.isMandatory, field?.disable, field?.component, field, sectionFormCategory)}
                  {field?.description && <CardText style={{ fontSize: "14px", marginTop: "-24px" }}>{t(field?.description)}</CardText>}
                  </div> */}
              </LabelFieldPair>

              {/* Migrating error message to field container as here it renders outside the field */}
              {/* {field?.populators?.name && errors && errors[field?.populators?.name] && Object.keys(errors[field?.populators?.name]).length ? (
                <ErrorMessage message={t(field?.populators?.error)} />
              ) : // {t(field?.populators?.error)}
              // </ErrorMessage>
              null} */}
            </Fragment>
          );
        })}
        {!props.noBreakLine && (array.length - 1 === index ? null : <BreakLine style={props?.breaklineStyle ? props?.breaklineStyle : {}} />)}
      </React.Fragment>
    ),
    [props.config, formData]
  );

  const getCardStyles = (shouldDisplay = true) => {
    let styles = props.cardStyle || {};
    if (props.noBoxShadow) styles = { ...styles, boxShadow: "none" };
    if (!shouldDisplay) styles = { ...styles, display: "none" };
    return styles;
  };

  const isDisabled = props.isDisabled || false;
  const checkKeyDown = (e) => {
    const keyCode = e.keyCode ? e.keyCode : e.key ? e.key : e.which;
    if (keyCode === 13) {
      // e.preventDefault();
    }
  };

  const setActiveNavByDefault = (configNav) => {
    let setActiveByDefaultRow = null;
    configNav?.forEach((row) => {
      if (row?.activeByDefault) {
        setActiveByDefaultRow = row;
      }
    });

    if (setActiveByDefaultRow) {
      return setActiveByDefaultRow?.name;
    }

    return configNav?.[0]?.name;
  };

  const [activeLink, setActiveLink] = useState(props.horizontalNavConfig ? setActiveNavByDefault(props.horizontalNavConfig) : null);

  useEffect(() => {
    setActiveLink(setActiveNavByDefault(props.horizontalNavConfig));
  }, [props.horizontalNavConfig]);

  const renderFormFields = (props, section, index, array, sectionFormCategory) => (
    <React.Fragment key={index}>
      {!props.childrenAtTheBottom && props.children}
      {props.heading && <Header style={{ ...props.headingStyle }}> {props.heading} </Header>}
      {props.description && <Header> {props.description} </Header>}
      {props.text && <p>{props.text}</p>}
      {formFields(section, index, array, sectionFormCategory)}
      {props.childrenAtTheBottom && props.children}
      {props.submitInForm && (
        <Button label={t(props.label)} style={{ ...props?.buttonStyle }} submit="submit" disabled={isDisabled} className="w-full" />
      )}
      {props.secondaryActionLabel && (
        <div className="primary-label-btn" style={{ margin: "20px auto 0 auto" }} onClick={onSecondayActionClick}>
          {props.secondaryActionLabel}
        </div>
      )}
    </React.Fragment>
  );

  return (
    <form onSubmit={handleSubmit(onSubmit)} onKeyDown={(e) => checkKeyDown(e)} id={props.formId} className={props.className}>
      {props?.showMultipleCardsWithoutNavs ? (
        props?.config?.map((section, index, array) => {
          return (
            !section.navLink && (
              <Card style={getCardStyles()} noCardStyle={props.noCardStyle} className={props.cardClassName}>
                {renderFormFields(props, section, index, array)}
              </Card>
            )
          );
        })
      ) : (
        <Card style={getCardStyles()} noCardStyle={props.noCardStyle} className={props.cardClassName}>
          {props?.config?.map((section, index, array) => {
            return !section.navLink && <>{renderFormFields(props, section, index, array)}</>;
          })}
        </Card>
      )}
      {props?.showFormInNav && props.horizontalNavConfig && (
        <HorizontalNav
          configNavItems={props.horizontalNavConfig ? props.horizontalNavConfig : null}
          showNav={props?.showNavs}
          activeLink={activeLink}
          setActiveLink={setActiveLink}
        >
          {props?.showMultipleCardsInNavs ? (
            props?.config?.map((section, index, array) => {
              return section.navLink ? (
                <Card style={section.navLink !== activeLink ? getCardStyles(false) : getCardStyles()} noCardStyle={props.noCardStyle}>
                  {renderFormFields(props, section, index, array, section?.sectionFormCategory)}
                </Card>
              ) : null;
            })
          ) : (
            <Card style={getCardStyles()} noCardStyle={props.noCardStyle}>
              {props?.config?.map((section, index, array) => {
                return section.navLink ? (
                  <>
                    <div style={section.navLink !== activeLink ? { display: "none" } : {}}>
                      {renderFormFields(props, section, index, array, section?.sectionFormCategory)}
                    </div>
                  </>
                ) : null;
              })}
            </Card>
          )}
        </HorizontalNav>
      )}
      {!props.submitInForm && props.label && (
        <ActionBar>
          <Button label={t(props.label)} submit="submit" disabled={isDisabled} />
          {props.onSkip && props.showSkip && <ActionLinks style={props?.skipStyle} label={t(`CS_SKIP_CONTINUE`)} onClick={props.onSkip} />}
        </ActionBar>
      )}
      {showErrorToast && <Toast error={true} label={t("ES_COMMON_PLEASE_ENTER_ALL_MANDATORY_FIELDS")} isDleteBtn={true} onClose={closeToast} />}
    </form>
  );
};
