import { Loader, Header, Toast, Card, Button,ActionBar,AddFilled, SubmitBar, CardLabelError } from "@egovernments/digit-ui-react-components";
import React, { useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import _ from "lodash";
import Form from "@rjsf/core";
import validator from "@rjsf/validator-ajv8";
// import { UiSchema } from '@rjsf/utils';
import { titleId } from "@rjsf/utils";
/*

created the foem using rjfs json form 

https://rjsf-team.github.io/react-jsonschema-form/docs/

*/

/*
The  DigitJSONForm  component is a custom form component built using the  react-jsonschema-form  library.
 It takes in a schema, an onSubmit function, an optional uiSchema, 
 a showToast and showErrorToast as props.  


*/
const uiSchema = {
  "ui:title": " ",
  "ui:classNames": "my-class",

  "ui:submitButtonOptions": {
    props: {
      disabled: false,
      className: "btn btn-info",
    },
    norender: true,
    submitText: "Submit",
  },
  "ui:submitButtonOptions": {
    props: {
      className: "object-jk",
    },
  },
 
};

function ArrayFieldItemTemplate(props) {
  const { children, className ,index,onDropIndexClick} = props;
  return (
    <div className={className}>
      {children}
{/* commented out since it has some issue 
      {props.hasRemove && (
        <button type="button" onClick={()=>onDropIndexClick(index)}>
          rem
        </button>
      )}
      */}
    </div>
  );
}

function TitleFieldTemplate(props) {
  console.log(props, "title");
  const { id, required, title } = props;
  return (
    <header id={id}>
      {title}
      {required && <mark>*</mark>}
    </header>
  );
}
function ArrayFieldTitleTemplate(props) {
  const { title, idSchema } = props;
  const id = titleId(idSchema);
  return null;
}
function ArrayFieldTemplate(props) {
  const { t } = useTranslation();
console.log(props,'propsarray');
  return (
    <div>
      {props.items.map((element,index) => {return (<span>
        <ArrayFieldItemTemplate {...element}></ArrayFieldItemTemplate>
      </span>)})}
      {props.canAdd && (
        <Button
        label={t("Add Eligibility Criteria")}
        variation="secondary"
        icon={<AddFilled style={{ height: "20px", width: "20px" }} />}
        onButtonClick={props.onAddClick}
        type="button"
      />
      )}
    </div>
  );
}

function ObjectFieldTemplate(props) {
  return (
    <div id={props?.idSchema?.["$id"]}>
      {/* {props.title} */}
      {props.description}
      {props.properties.map((element) => {
        console.log(element, "object");
        return (
          <div className="field-wrapper" id={`${props?.idSchema?.["$id"]}_${element.name}`}>
            {element.content}
          </div>
        );
      })}
    </div>
  );
}

function CustomFieldTemplate(props) {
  const { id, classNames, style, label, help, required, description, errors, children } = props;
  console.log(props,"all")
  return (
    <span>
    <div className={classNames} style={style}>
      <label htmlFor={id} className="control-label">
        {label}
        {required ? "*" : null}
      </label>
      {description}
      <span>{children}
      {errors}
      {help}
      </span>
    </div>
    </span>
  );
}

const FieldErrorTemplate = (props) => {
  const { errors } = props;
  console.log("errors", errors);
  return errors && errors.length > 0 && errors?.[0] ? <CardLabelError>{errors?.[0]}</CardLabelError> : null;
};

const DigitJSONForm = ({ schema, onSubmit, uiSchema: inputUiSchema, showToast, showErrorToast,formData={} ,onFormChange,onFormError}) => {
  const { t } = useTranslation();

  const onSubmitV2 = ({ formData }) => {
    onSubmit(formData);
    console.log("Data submitted: ", formData);
  };

  return (
    <React.Fragment>
      <Header className="digit-form-composer-header">{t("WBH_ADD_MDMS")}</Header>
      <Card className="workbench-create-form">
        <Header className="digit-form-composer-sub-header">{t(Digit.Utils.workbench.getMDMSLabel(schema?.code))}</Header>
        <Form
          schema={schema?.definition}
          validator={validator}
          liveValidate
          focusOnFirstError={true}
          showErrorList={false}
          formData={formData}
          noHtml5Validate={true}
          onChange={onFormChange}
          onSubmit={onSubmitV2}
          templates={{
            FieldErrorTemplate,
            ArrayFieldTemplate,
            FieldTemplate: CustomFieldTemplate,
            ObjectFieldTemplate,
            TitleFieldTemplate,
            ArrayFieldTitleTemplate,
            ArrayFieldItemTemplate,
          }}
          experimental_defaultFormStateBehavior={{
            arrayMinItems: { populate: "requiredOnly" },
          }}
          uiSchema={{ ...uiSchema, ...inputUiSchema }}
          onError={onFormError}
        >
          <ActionBar>
            <SubmitBar label={t("WBH_ADD_MDMS_ADD_ACTION")} submit="submit" />
            {/* <LinkButton style={props?.skipStyle} label={t(`CS_SKIP_CONTINUE`)}  /> */}
          </ActionBar>
        </Form>
      </Card>
      {showToast && <Toast label={t(showToast)} error={showErrorToast}></Toast>}
    </React.Fragment>
  );
};

export default DigitJSONForm;
