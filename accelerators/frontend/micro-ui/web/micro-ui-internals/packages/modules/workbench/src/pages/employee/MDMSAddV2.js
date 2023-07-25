import { Loader } from "@egovernments/digit-ui-react-components";
import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { DigitJSONForm } from "../../Module";

/*

created the foem using rjfs json form 

https://rjsf-team.github.io/react-jsonschema-form/docs/

*/

const uiSchema = {};

const MDMSAdd = ({ FormSession }) => {
  // const tenantId = Digit.ULBService.getCurrentTenantId();
  const stateId = Digit.ULBService.getStateId();
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
      }, 1500);
      setShowToast(`Success ::  ${resp?.mdms?.[0]?.id}`);
    };
    const onError = (resp) => {
      setShowToast(`Error :: ${resp?.response?.data?.Errors?.[0]?.code}`);
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

  /* use newConfig instead of commonFields for local development in case needed */
  if (isLoading) {
    return <Loader />;
  }

  return (
    <DigitJSONForm schema={schema} onSubmit={onSubmit} uiSchema={uiSchema} showToast={showToast} showErrorToast={showErrorToast}></DigitJSONForm>
  );
};

export default MDMSAdd;
