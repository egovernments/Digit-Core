import {
  Loader,
  Header,
  Toast,
  Card,
  Button,
  ActionBar,
  AddFilled,
  SubmitBar,
  CardLabelError,
  SVG,
  Menu,
  CollapseAndExpandGroups,
} from "@egovernments/digit-ui-react-components";
import React, { useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import _ from "lodash";
import Form from "@rjsf/core";
import validator from "@rjsf/validator-ajv8";
// import { UiSchema } from '@rjsf/utils';
import { titleId } from "@rjsf/utils";
import App from "./MultiSelect";
import { CustomCheckbox } from "./checbox";
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

function transformErrors(errors) {
  const { t } = this;
  console.log(errors, "errors");
  // Custom validation logic for all widgets
  // You can modify or add error messages based on your requirements
  return errors.map((error) => {
    error.message = t(Digit.Utils.workbench.getMDMSLabel(`WBH_ERROR_${error?.name}`));
    if (error?.name === "pattern") {
      error.message += ` : ${error?.params?.pattern}`;
    }
    // if (error.property === '.name' && error.name === 'minLength') {
    //   error.message = 'Name must be at least 3 characters';
    // }
    // if (error.property === '.email' && error.name === 'format') {
    //   error.message = 'Invalid email format';
    // }
    return error;
  });
}
const getArrayWrapperClassName=(type)=>{
  switch(type){
    case "array":
      return "jk-array-of-array";
    case "object":
      return "jk-array-objects";
    default:
      return "jk-array-of-non-objects";
  }
  
}

function ArrayFieldItemTemplate(props) {
  const { t } = useTranslation();

  const { children, className, index, onDropIndexClick ,schema,disabled} = props;
  const isArrayOfObjects=schema?.type== "object";
  const newClass=getArrayWrapperClassName(schema?.type)
  return (
    <div className={`${className} ${newClass}`}>
     <span className={"array-children"}>
     {children}
     </span>
      {isArrayOfObjects ? <span className="array-obj">
      {props.hasRemove && (
        <div className="array-remove-button-wrapper">
            <Button
          label={`${t("WBH_DELETE_ACTION")}`}
          variation="secondary"
          className="array-remove-button" 
          icon={ <SVG.Delete width={"28"} height={"28"} />}
          onButtonClick={onDropIndexClick(index)}
          type="button"
          isDisabled={disabled}
        />
        </div>
      )}
        </span>:
      props.hasRemove && (
        <div className="array-remove-button-wrapper">
            <Button
          label={`${t("WBH_DELETE_ACTION")}`}
          variation="secondary"
          className="array-remove-button" 
          icon={ <SVG.Delete width={"28"} height={"28"} />}
          onButtonClick={onDropIndexClick(index)}
          type="button"
          isDisabled={disabled}
        />
        </div>
      )}
    </div>
  );
}

function TitleFieldTemplate(props) {
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
  if (props?.required && !props?.schema?.minItems) {
    props.schema.minItems = 1;
  }

  return (
    <div className="array-wrapper">
      {props.items.map((element, index) => {
        return (
          <span className="array-element-wrapper">
            <ArrayFieldItemTemplate title={props?.title} key={index} index={index} {...element}></ArrayFieldItemTemplate>
          </span>
        );
      })}
      {props.canAdd && (
        <Button
          label={`${t(`WBH_ADD`)} ${t(props?.title)}`}
          variation="secondary"
          icon={<AddFilled style={{ height: "20px", width: "20px" }} />}
          onButtonClick={props.onAddClick}
          type="button"
          isDisabled={props.disabled}
        />
      )}
    </div>
  );
}

function ObjectFieldTemplate(props) {
  const children = props.properties.map((element) => {
    return (
      <div className="field-wrapper object-wrapper" id={`${props?.idSchema?.["$id"]}_${element.name}`}>
        {element.content}
      </div>
    );
  });
  const isRoot = props?.["idSchema"]?.["$id"] == "digit_root";

  return (
    <div id={props?.idSchema?.["$id"]}>
      {/* {props.title} */}
      {props.description}

      {isRoot ? (
        children
      ) : (
        <CollapseAndExpandGroups showHelper={true} groupHeader={""} groupElements={true} children={children}></CollapseAndExpandGroups>
      )}
    </div>
  );
}

function CustomFieldTemplate(props) {
  const { t } = useTranslation();
  const { moduleName, masterName } = Digit.Hooks.useQueryParams();
  const { id, classNames, style, label, help, required, description, errors, children } = props;
  let titleCode = label;
  let additionalCode = "";
  if (!label?.includes(Digit.Utils.locale.getTransformedLocale(moduleName)) && !label?.includes(Digit.Utils.locale.getTransformedLocale(masterName))) {
    titleCode = Digit.Utils.locale.getTransformedLocale(`${moduleName}.${moduleName}_${label?.slice(0, -2)}`);
    additionalCode = label?.slice(-2);
  }
  return (
    <span>
      <div className={classNames} style={style}>
        <label htmlFor={id} className="control-label" id={"label_" + id}>
          {/* <span >{label}</span> */}
          <span className={`tooltip`}>
            {t(titleCode)} {additionalCode}
            <span className="tooltiptext">
              <span className="tooltiptextvalue">{t(`TIP_${titleCode}`)}</span>
            </span>
          </span>
          {required ? "*" : null}
        </label>
        {description}
        <span class="all-input-field-wrapper">
          {children}
          {errors}
          {help}
        </span>
      </div>
    </span>
  );
}

const FieldErrorTemplate = (props) => {
  const { errors } = props;
  return errors && errors.length > 0 && errors?.[0] ? <CardLabelError>{errors?.[0]}</CardLabelError> : null;
};

const DigitJSONForm = ({
  schema,
  onSubmit,
  uiSchema: inputUiSchema,
  showToast,
  showErrorToast,
  formData = {},
  onFormChange,
  onFormError,
  screenType = "add",
  onViewActionsSelect,
  viewActions,
  disabled = false,
  setShowToast,
  setShowErrorToast
}) => {
  const { t } = useTranslation();

  const onSubmitV2 = ({ formData }) => {
    onSubmit(formData);
  };
  const customWidgets = { SelectWidget: App, CheckboxWidget: CustomCheckbox };

  const [displayMenu, setDisplayMenu] = useState(false);
  const [liveValidate, setLiveValidate] = useState(false);
  const onError = (errors) => {
    setLiveValidate(true);
    onFormError(errors);
  };
  const person = { t: t };

  return (
    <React.Fragment>
      <Header className="digit-form-composer-header">
        {screenType === "add" ? t("WBH_ADD_MDMS") : screenType === "view" ? t("WBH_VIEW_MDMS") : t("WBH_EDIT_MDMS")}
      </Header>
      <Card className="workbench-create-form">
        <Header className="digit-form-composer-sub-header">{t(Digit.Utils.workbench.getMDMSLabel(`SCHEMA_` + schema?.code))}</Header>
        <Form
          schema={schema?.definition}
          validator={validator}
          showErrorList={false}
          formData={formData}
          noHtml5Validate={true}
          onChange={onFormChange}
          onSubmit={onSubmitV2}
          idPrefix="digit_root"
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
          widgets={customWidgets}
          transformErrors={transformErrors.bind(person)}
          uiSchema={{ ...uiSchema, ...inputUiSchema }}
          onError={onError}
          disabled={disabled}
          // disabled the error onload
          // focusOnFirstError={true}
          /* added logic to show live validations after form submit is clicked */
          liveValidate={liveValidate}
          // liveValidate={formData && Object.keys(formData) && Object.keys(formData)?.length > 0}
        >
          {(screenType === "add" || screenType === "edit") && (
            <ActionBar style={{zIndex:"0"}}>
              <SubmitBar label={screenType === "edit" ? t("WBH_ADD_MDMS_UPDATE_ACTION") : t("WBH_ADD_MDMS_ADD_ACTION")} submit="submit" />
              {/* <LinkButton style={props?.skipStyle} label={t(`CS_SKIP_CONTINUE`)}  /> */}
            </ActionBar>
          )}
          {screenType === "view" && (
            <ActionBar>
              {displayMenu ? (
                <Menu
                  localeKeyPrefix={""}
                  options={viewActions}
                  optionKey={"label"}
                  t={t}
                  onSelect={onViewActionsSelect}
                  textStyles={{ margin: "0px" }}
                />
              ) : null}
              <SubmitBar label={t("WORKS_ACTIONS")} onSubmit={() => setDisplayMenu(!displayMenu)} />
            </ActionBar>
          )}
        </Form>
      </Card>
      {showToast && <Toast label={t(showToast)} error={showErrorToast} onClose={()=>{
        setShowToast(null)
        }} isDleteBtn={true}></Toast> }
    </React.Fragment>
  );
};

export default DigitJSONForm;
