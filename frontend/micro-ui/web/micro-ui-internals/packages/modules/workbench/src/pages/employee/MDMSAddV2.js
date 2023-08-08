import { Loader } from "@egovernments/digit-ui-react-components";
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

const MDMSAdd = ({defaultFormData,updatesToUISchema,screenType="add",onViewActionsSelect,viewActions,onSubmitEditAction,...props}) => {
  
  // const tenantId = Digit.ULBService.getCurrentTenantId();
  const stateId = Digit.ULBService.getStateId();
  const FormSession = Digit.Hooks.useSessionStorage(`MDMS_${screenType}`, {});

  const [sessionFormData, setSessionFormData, clearSessionFormData] = FormSession;
  const [session, setSession] = useState(sessionFormData);
  const [formSchema, setFormSchema] = useState({});
  const [loadDependent, setLoadDependent] = useState("");
  const [showErrorToast, setShowErrorToast] = useState(false);
  
  const [showToast, setShowToast] = useState(false);
  const { moduleName, masterName } = Digit.Hooks.useQueryParams();
  
  useEffect(() => {
    setSession({  ...defaultFormData });
  }, [defaultFormData]);

  const { t } = useTranslation();
  const history = useHistory();
  const reqCriteria = {
    url: "/mdms-v2/schema/v1/_search",
    params: {},
    body: {
      SchemaDefCriteria: {
        tenantId:  stateId,
        codes: [`${moduleName}.${masterName}`],
      },
    },
    config: {
      enabled: moduleName && masterName && true,
      select: (data) => {
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
        tenantId:  stateId,
        schemaCodes: [loadDependent?.schemaCode],
      },
    },
    config: {
      enabled: loadDependent == "" ? false : true,
      select: (data) => {
        return data?.mdms?.map((ele) => ele.uniqueIdentifier);
      },
    },
    changeQueryName: "data",
  };
  const reqCriteriaAdd = {
    url: `/mdms-v2/v2/_create/${moduleName}.${masterName}`,
    params: {},
    body: {
      Mdms: {
        tenantId:  stateId,
        schemaCode: `${moduleName}.${masterName}`,
        uniqueIdentifier: null,
        data: {},
        isActive: true,
      },
    },
    config: {
      enabled: true,
      select: (data) => {
        return data?.SchemaDefinitions?.[0] || {};
      },
    },
  };
  const { isLoading: additonalLoading, data: additonalData } = Digit.Hooks.useCustomAPIHook(reqCriteriaForData);
  const { isLoading, data: schema, isFetching } = Digit.Hooks.useCustomAPIHook(reqCriteria);

  const mutation = Digit.Hooks.useCustomAPIMutationHook(reqCriteriaAdd);
  const onSubmit = (data) => {
    // const formattedData = Digit.Utils.workbench.getFormattedData(data);
    const onSuccess = (resp) => {
      setTimeout(() => {
        setSessionFormData({});
        setSession({});
      }, 1500);
      setShowErrorToast(false);
      setShowToast(`${t('WBH_SUCCESS_MDMS_MSG')} ${resp?.mdms?.[0]?.id}`);
    };
    const onError = (resp) => {
      setShowToast(`${t('WBH_ERROR_MDMS_DATA')}  ${resp?.response?.data?.Errors?.[0]?.code}`);
      setShowErrorToast(true);
    };

    mutation.mutate(
      {
        params: {},
        body: {
          Mdms: {
            tenantId: stateId,
            schemaCode: `${moduleName}.${masterName}`,
            uniqueIdentifier: null,
            data: { ...data },
            isActive: true,
          },
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
          if(schema){
Object.keys(schema?.definition?.properties).map(key=>{
  const title=Digit.Utils.locale.getTransformedLocale(`${schema?.code}_${key}`)
  schema.definition.properties[key]={...schema.definition.properties[key],title:t(title)}
})
setFormSchema(schema)
    if (schema?.definition?.["x-ref-schema"]?.length > 0) {
      schema?.definition?.["x-ref-schema"]?.map((ele) => {
        setLoadDependent(ele);
      });
    }
  }
  }, [schema]);

  useEffect(() => {
    if (loadDependent?.fieldPath && additonalData?.length > 0) {
      if (schema?.definition?.properties?.[loadDependent?.fieldPath]) {
        schema.definition.properties[loadDependent.fieldPath] = { ...schema.definition.properties[loadDependent.fieldPath], enum: additonalData };
        schema.definition.properties["temp_field"] = { ...schema.definition.properties[loadDependent.fieldPath], enum: additonalData };
        setFormSchema({ ...schema });
        setTimeout(() => {
          setFormSchema((schema) => {
            delete schema.definition.properties["temp_field"];
            return { ...schema };
          }, 500);
        });
      }
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

  /* use newConfig instead of commonFields for local development in case needed */
  if (isLoading || !formSchema||Object.keys(formSchema)==0) {
    return <Loader />;
  }

  return (
    <React.Fragment>
      {formSchema && (
        <DigitJSONForm
          schema={formSchema}
          onFormChange={onFormValueChange}
          onFormError={onFormError}
          formData={session}
          onSubmit={screenType==="add" ? onSubmit : onSubmitEditAction}
          uiSchema={{ ...uiSchema, ...updatesToUISchema }}
          showToast={showToast}
          showErrorToast={showErrorToast}
          screenType={screenType}
          viewActions={viewActions}
          onViewActionsSelect={onViewActionsSelect}
        ></DigitJSONForm>
      )}
    </React.Fragment>
  );
};

export default MDMSAdd;
