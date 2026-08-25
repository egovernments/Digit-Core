import { Card, Loader, SVG } from "@egovernments/digit-ui-react-components";
import React, { useState, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { DigitJSONForm } from "../../Module";
import _ from "lodash";
import { DigitLoader } from "../../components/DigitLoader";

/*

created the foem using rjfs json form 

https://rjsf-team.github.io/react-jsonschema-form/docs/

*/
const onFormError = (errors) => console.log("I have", errors.length, "errors to fix");

const uiSchema = {};

const MDMSAdd = ({ defaultFormData, updatesToUISchema, screenType = "add", onViewActionsSelect, viewActions, onSubmitEditAction, ...props }) => {
  const tenantId = Digit.ULBService.getCurrentTenantId();
  const [spinner, toggleSpinner] = useState(false);
  // const stateId = Digit.ULBService.getStateId();
  const FormSession = Digit.Hooks.useSessionStorage(`MDMS_${screenType}`, {});
  const [sessionFormData, setSessionFormData, clearSessionFormData] = FormSession;
  const [session, setSession] = useState(sessionFormData);
  const [formSchema, setFormSchema] = useState({});
  const [api, setAPI] = useState(false);

  const [noSchema, setNoSchema] = useState(false);
  const [loadDependent, setLoadDependent] = useState([]);
  const [showErrorToast, setShowErrorToast] = useState(false);
  const [disableForm, setDisableForm] = useState(false);

  const [showToast, setShowToast] = useState(false);
  const { moduleName, masterName } = Digit.Hooks.useQueryParams();

  useEffect(() => {
    setSession({ ...session, ...defaultFormData });
  }, [defaultFormData]);

  const { t } = useTranslation();
  const history = useHistory();
  const reqCriteria = {
    url: "/mdms-v2/schema/v1/_search",
    params: {},
    body: {
      SchemaDefCriteria: {
        tenantId: tenantId,
        codes: [`${moduleName}.${masterName}`],
      },
    },
    config: {
      enabled: moduleName && masterName && true,
      select: (data) => {
        if (data?.SchemaDefinitions?.length == 0) {
          setNoSchema(true);
        }
        if (data?.SchemaDefinitions?.[0]?.definition?.["x-ui-schema"]?.["ui-apidetails"]) {
          setAPI(data?.SchemaDefinitions?.[0]?.definition?.["x-ui-schema"]?.["ui-apidetails"]);
        }
        return data?.SchemaDefinitions?.[0] || {};
      },
    },
    changeQueryName: "schema",
  };
  const reqCriteriaForData = {
    url: `/mdms-v2/v2/_search`,
    params: {},
    body: {
      MdmsCriteria: {
        tenantId: tenantId,
        schemaCodes: loadDependent.map((e) => e.schemaCode),
      },
    },
    config: {
      enabled: loadDependent && loadDependent?.length > 0,
      select: (data) => {
        const dependentData = {};
        data?.mdms?.map((ele) => {
          if (dependentData?.[ele?.schemaCode] && dependentData?.[ele?.schemaCode]?.length > 0) {
            dependentData[ele?.schemaCode]?.push(ele?.uniqueIdentifier);
          } else {
            dependentData[ele?.schemaCode] = [ele?.uniqueIdentifier];
          }
        });
        return dependentData;
      },
    },
    changeQueryName: "data",
  };

  const { isLoading: additonalLoading, data: additonalData } = Digit.Hooks.useCustomAPIHook(reqCriteriaForData);
  const { isLoading, data: schema, isFetching } = Digit.Hooks.useCustomAPIHook(reqCriteria);
  const body = api?.requestBody
    ? { ...api?.requestBody }
    : {
        Mdms: {
          tenantId: tenantId,
          schemaCode: `${moduleName}.${masterName}`,
          uniqueIdentifier: null,
          data: {},
          isActive: true,
        },
      };
  const reqCriteriaAdd = {
    url: api ? api?.url : `/mdms-v2/v2/_create/${moduleName}.${masterName}`,
    params: {},
    body: { ...body },
    config: {
      enabled: schema ? true : false,
      select: (data) => {
        return data?.SchemaDefinitions?.[0] || {};
      },
    },
  };

  const mutation = Digit.Hooks.useCustomAPIMutationHook(reqCriteriaAdd);
  const onSubmit = (data) => {
    toggleSpinner(true);
    const onSuccess = (resp) => {
      toggleSpinner(false);
      setSessionFormData({});
      setSession({});
      setShowErrorToast(false);
      const jsonPath = api?.responseJson ? api?.responseJson : "mdms[0].id";
      setShowToast(`${t("WBH_SUCCESS_MDMS_MSG")} ${_.get(resp, jsonPath, "NA")}`);
      closeToast();

      //here redirect to search screen(check if it's required cos user might want  add multiple masters in one go)
    };
    const onError = (resp) => {
      toggleSpinner(false);
      setShowToast(`${t("WBH_ERROR_MDMS_DATA")} ${t(resp?.response?.data?.Errors?.[0]?.code)}`);
      setShowErrorToast(true);
      closeToast();
    };

    _.set(body, api?.requestJson ? api?.requestJson : "Mdms.data", { ...data });
    mutation.mutate(
      {
        params: {},
        body: {
          ...body,
        },
      },
      {
        onError,
        onSuccess,
      }
    );
  };
  const onFormValueChange = (updatedSchema, element) => {
    const { formData } = updatedSchema;

    if (!_.isEqual(session, formData)) {
      setSession({ ...session, ...formData });
    }
  };

  useEffect(() => {
    // setFormSchema(schema);
    /* localise */
    if (schema && schema?.definition) {
      // Object.keys(schema?.definition?.properties).map((key) => {
      //   const title = Digit.Utils.locale.getTransformedLocale(`${schema?.code}_${key}`);
      //   schema.definition.properties[key] = { ...schema.definition.properties[key], title: t(title) };
      //   console.log(schema.definition.properties[key],'schema.definition.properties[key]',key)
      // });
      Digit.Utils.workbench.updateTitleToLocalisationCodeForObject(schema?.definition, schema?.code);
      setFormSchema(schema);
      /* logic to search for the reference data from the mdms data api */

      if (schema?.definition?.["x-ref-schema"]?.length > 0) {
        setLoadDependent([...schema?.definition?.["x-ref-schema"]]);
      }
    }
  }, [schema]);

  useEffect(() => {
    /* logic to enable the enum values fetched from the mdms data api */
    if (loadDependent && loadDependent?.length > 0) {
      loadDependent?.map((dependent) => {
        if (dependent?.fieldPath && additonalData?.[dependent?.schemaCode]?.length > 0) {
          let updatedPath = Digit.Utils.workbench.getUpdatedPath(dependent?.fieldPath);
          if (_.get(schema?.definition?.properties, updatedPath)) {
            _.set(schema?.definition?.properties, updatedPath, {
              ..._.get(schema?.definition?.properties, updatedPath, {}),
              enum: additonalData?.[dependent?.schemaCode],
              schemaCode : dependent?.schemaCode
            });
          }
        }
      });
      setFormSchema({ ...schema });
      /* added disable to get the complete form re rendered to get the enum values reflected */
      setDisableForm(true);
      setTimeout(() => {
        setDisableForm(false);
      });
    }
  }, [additonalData]);

  useEffect(() => {
    if (!_.isEqual(sessionFormData, session)) {
      const timer = setTimeout(() => {
        setSessionFormData({ ...sessionFormData, ...session });
      }, 1000);
      return () => {
        clearTimeout(timer);
      };
    }
  }, [session]);

  if (noSchema) {
    return (
      <Card>
        <span className="workbench-no-schema-found">
          <h4>{t("WBH_NO_SCHEMA_FOUND")}</h4>
          <SVG.NoResultsFoundIcon width="20em" height={"20em"} />
        </span>
      </Card>
    );
  }

  const closeToast = () => {
    setTimeout(() => {
      setShowToast(null);
    }, 5000);
  };

  /* use newConfig instead of commonFields for local development in case needed */
  if (isLoading || !formSchema || Object.keys(formSchema) == 0) {
    return <Loader />;
  }
  const uiJSONSchema = formSchema?.["definition"]?.["x-ui-schema"];

  return (
    <React.Fragment>
      {spinner && <DigitLoader />}
      {formSchema && (
        <DigitJSONForm
          schema={formSchema}
          onFormChange={onFormValueChange}
          onFormError={onFormError}
          formData={session}
          onSubmit={screenType === "add" ? onSubmit : onSubmitEditAction}
          uiSchema={{ ...uiSchema, ...uiJSONSchema, ...updatesToUISchema }}
          showToast={showToast}
          closeToast={closeToast}
          setShowToast={setShowToast}
          showErrorToast={showErrorToast}
          setShowErrorToast={setShowErrorToast}
          screenType={screenType}
          viewActions={viewActions}
          onViewActionsSelect={onViewActionsSelect}
          disabled={disableForm}
        ></DigitJSONForm>
      )}
    </React.Fragment>
  );
};

export default MDMSAdd;
