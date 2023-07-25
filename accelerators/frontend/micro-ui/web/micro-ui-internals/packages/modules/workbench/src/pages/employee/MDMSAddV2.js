import { Loader, Header, FormComposerV2, Toast, Card, ActionBar, SubmitBar } from "@egovernments/digit-ui-react-components";
import React, { useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { mdmsSchema } from "../../configs/sampleschema";
import _ from "lodash";
import { useHistory, useParams } from "react-router-dom";
import Form from "@rjsf/core";
import validator from "@rjsf/validator-ajv8";
// import { UiSchema } from '@rjsf/utils';

/*

created the foem using rjfs json form 

https://rjsf-team.github.io/react-jsonschema-form/docs/

*/

const uiSchema = {
  "ui:submitButtonOptions": {
    props: {
      disabled: false,
      className: "btn btn-info",
    },
    norender: true,
    submitText: "Submit",
  },
};

const MDMSAdd = ({ FormSession }) => {
  // const tenantId = Digit.ULBService.getCurrentTenantId();
  const stateId = Digit.ULBService.getStateId();
  const [showErrorToast, setShowErrorToast] = useState(false);

  const [showToast, setShowToast] = useState(false);
  const { moduleName, masterName, tenantId } = Digit.Hooks.useQueryParams();
  const [sessionFormData, setSessionFormData, clearSessionFormData] = FormSession;

  const [session, setSession] = useState(sessionFormData);

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
      setShowErrorToast(`Error :: ${resp?.response?.data?.Errors?.[0]?.code}`);
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
  const onFormValueChange = (setValue, formData, formState) => {
    // if (!_.isEqual(sessionFormData, formData)) {
    //   // const result = _.pickBy(sessionFormData, (v, k) => !_.isEqual(formData[k], v));
    //   /* update session if any dependency */
    //   // if (result?.["dependencyField"]) {
    //   //   setValue("dependentField", );
    //   // }
    //   setSessionFormData({ ...sessionFormData, ...formData });
    // }
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
    return <Loader />;
  }

  const configs = Digit.Hooks.workbench.UICreateConfigGenerator(schema, {});
  const onSubmitV2 = ({ formData }) => {
    onSubmit(formData);
    console.log("Data submitted: ", formData);
  };

  return (
    <React.Fragment>
      <Header className="digit-form-composer-header">{t("WBH_ADD_MDMS")}</Header>
      <Card className="workbench-create-form">
        <Form
          schema={schema?.definition}
          validator={validator}
          onChange={console.log("changed")}
          onSubmit={onSubmitV2}
          uiSchema={uiSchema}
          onError={console.log("errors")}
        >
          <ActionBar>
            <SubmitBar label={t("WBH_ADD_MDMS_ADD_ACTION")} submit="submit" />
            {/* <LinkButton style={props?.skipStyle} label={t(`CS_SKIP_CONTINUE`)}  /> */}
          </ActionBar>
        </Form>
      </Card>
      {(showToast || showErrorToast) && <Toast label={t(showToast || showErrorToast)} error={showErrorToast && true}></Toast>}
    </React.Fragment>
    // <FormComposerV2
    //   headerLabel="WBH_ADD_MDMS"
    //   label={t("WBH_ADD_MDMS_ADD_ACTION")}
    //   description={""}
    //   text={""}
    //   config={configs.map((config) => {
    //     return {
    //       ...config,
    //       body: config.body.filter((a) => !a.hideInEmployee),
    //     };
    //   })}
    //   customToast={showToast ? { error: false, label: showToast } : showErrorToast ? { error: true, label: showErrorToast } : null}
    //   updateCustomToast={()=>setShowToast(false)}
    //   defaultValues={session}
    //   onFormValueChange={onFormValueChange}
    //   onSubmit={onSubmit}
    //   fieldStyle={{ marginRight: 0 }}
    //   jsonSchema={schema?.definition}
    // />
  );
};

export default MDMSAdd;
