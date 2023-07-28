import { Loader } from "@egovernments/digit-ui-react-components";
import React, { useState,useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { DigitJSONForm } from "../../Module";
import _ from "lodash";
import { DigitLoader } from "../../components/DigitLoader";

/*

created the foem using rjfs json form 

https://rjsf-team.github.io/react-jsonschema-form/docs/

*/
const onFormError = (errors) => console.log('I have', errors.length, 'errors to fix');

const uiSchema = {};

const MDMSAdd = () => {
  // const tenantId = Digit.ULBService.getCurrentTenantId();
  const stateId = Digit.ULBService.getStateId();
  const FormSession = Digit.Hooks.useSessionStorage("MDMS_CREATE", {});

  const [sessionFormData, setSessionFormData, clearSessionFormData] = FormSession;
  const [session, setSession] = useState(sessionFormData);

  const [showErrorToast, setShowErrorToast] = useState(false);

  const [showToast, setShowToast] = useState(false);
  const { moduleName, masterName, tenantId } = Digit.Hooks.useQueryParams();

  const { t } = useTranslation();
  const history = useHistory();
  const reqCriteria = {
    url: "/mdms-v2/schema/v1/_search",
    params: {},
    body: {
      SchemaDefCriteria: {
        tenantId: tenantId || stateId,
        codes: [`${moduleName}.${masterName}`],
      },
    },
    config: {
      enabled: moduleName && masterName && true,
      select: (data) => {
        return data?.SchemaDefinitions?.[0] || {};
      },
    },
  };
  const reqCriteriaAdd = {
    url: `/mdms-v2/v2/_create/${moduleName}.${masterName}`,
    params: {},
    body: {
      Mdms: {
        tenantId: stateId,
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
      setShowToast(`Success : Data added Successfully with Id : ${resp?.mdms?.[0]?.id}`);
    };
    const onError = (resp) => {
      setShowToast(`Error : Following error occured ${resp?.response?.data?.Errors?.[0]?.code}`);
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
  const onFormValueChange = (updatedSchema,element) => {
    const {formData} = updatedSchema;
   
    if (!_.isEqual(session, formData)) {
      setSession({ ...session, ...formData });
    }
  };





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
  if (isLoading) {
    return <Loader/>;
  }

console.log(showToast,'showToast')
  
  return (
    <React.Fragment>
      {/* <DigitLoader /> */}
    
    <DigitJSONForm schema={schema} onFormChange={onFormValueChange} onFormError={onFormError} 
    formData={session} 
    onSubmit={onSubmit} uiSchema={uiSchema} showToast={showToast} showErrorToast={showErrorToast}></DigitJSONForm>
    </React.Fragment>
  );
};

export default MDMSAdd;
