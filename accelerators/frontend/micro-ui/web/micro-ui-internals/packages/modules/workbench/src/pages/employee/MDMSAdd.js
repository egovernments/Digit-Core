import { Loader, FormComposerV2 } from "@egovernments/digit-ui-react-components";
import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { mdmsSchema } from "../../configs/sampleschema";
import _ from "lodash";


const MDMSAdd = ({ FormSession }) => {
  const tenantId = Digit.ULBService.getCurrentTenantId();
  const [sessionFormData, setSessionFormData, clearSessionFormData] = FormSession;

  const [session, setSession] = useState(sessionFormData);

  const { t } = useTranslation();
  const history = useHistory();

  const onSubmit = (data) => {
    const formattedData = Digit.Utils.workbench.getFormattedData(data);
    console.log(formattedData,"entered data")
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

  const configs = Digit.Hooks.workbench.UICreateConfigGenerator(mdmsSchema, {});

  return (
    <FormComposerV2
      heading={t("WBH_ADD_MDMS")}
      label={t("WBH_ADD_MDMS_ADD_ACTION")}
      description={""}
      text={""}
      config={configs.map((config) => {
        return {
          ...config,
          body: config.body.filter((a) => !a.hideInEmployee),
        };
      })}
      defaultValues={session}
      onFormValueChange={onFormValueChange}
      onSubmit={onSubmit}
      fieldStyle={{ marginRight: 0 }}
    />
  );
};

export default MDMSAdd;
