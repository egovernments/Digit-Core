import { Loader, FormComposerV2 } from "@egovernments/digit-ui-react-components";
import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import ProjectComponent from "../../components/ProjectComponent";

export const newConfig = [
  {
    head: "Create Product",
    body: [
      {
        label: "Type",
        isMandatory: true,
        type: "text",
        disable: false,
        populators: { name: "type", error: "Required" },
      },
      {
        label: "Name",
        isMandatory: true,
        type: "text",
        disable: false,
        populators: { name: "name", error: "Required" },
      },
      {
        label: "Manufacturer",
        isMandatory: true,
        type: "text",
        disable: false,
        populators: { name: "manufacturer", error: "Required" },
      },
      {
        label: "Schema",
        isMandatory: true,
        type: "text",
        disable: false,
        populators: { name: "schema", error: "Required" },
      },
      {
        label: "Version",
        isMandatory: true,
        type: "number",
        disable: false,
        populators: { name: "version", error: "Required" },
      },
      {
        label: "Form",
        isMandatory: true,
        type: "text",
        disable: false,
        populators: { name: "form", error: "Required" },
      },
      {
        label: "Is Deleted",
        isMandatory: false,
        type: "checkbox",
        disable: false,
        populators: { name: "isDeleted", error: "Required" },
      },
      {
        "type": "component",
        "component": "ProjectComponent",
        "withoutLabel": true,
        "key": "comments"
      },
    ],
  },
];

const Create = () => {
  const [setPropFormData,porpFormData]= React.useState('');
  const tenantId = Digit.ULBService.getCurrentTenantId();
  const { t } = useTranslation();
  const history = useHistory();

  const onSubmit = (formData) => {
    console.log("formdata is"+ formData)
    setPropFormData(formData)

      Digit.PackageService.create(formData, tenantId).then((result,err)=>{
        return result.json();
      })
      .then(()=>{
        console.log("data is",data)
      })
      .catch((e) => {
      console.log("err");
     });
  };

  const configs = newConfig ? newConfig : newConfig;

  return (
    <FormComposerV2
      heading={t("Application Heading")}
      label={t("Submit")}
      description={"Description"}
      text={"Sample Text if required"}
      config={configs.map((config) => {
        return {
          ...config,
          body: config.body.filter((a) => !a.hideInEmployee),
        };
      })}
      defaultValues={{}}
      onSubmit={onSubmit}
      fieldStyle={{ marginRight: 0 }}
    />
  );
};

export default Create;
