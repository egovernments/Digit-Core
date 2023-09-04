import React from "react";
// import Fields from "../Fields";
import FieldComposer from "../FieldComposer";

export default {
  title: "Atom-Groups/Fields",
  component: FieldComposer,
  argTypes: {
    type: {
      control: {
        type: "select",
        options: [
          "date",
          "text",
          "number",
          "password",
          "time",
          "amount",
          "textarea",
          "paragraph",
          "mobileNumber",
          "custom",
          "checkbox",
          "multiupload",
          "select",
          "radio",
          "dropdown",
          "radioordropdown",
          "component",
          "documentUpload",
          "form",
          "locationdropdown",
          "apidropdown",
          "multiselectdropdown",
        ],
      },
    },
    variant: {
      control: {
        type: "select",
        options: ["default", "error", "focused", "disabled"],
      },
    },
  },
};

const Template = (args) => <FieldComposer type={args?.type} {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  type: args?.type,
  populators: {
    name: "Label",
    error: "Required",
    validation: {
      pattern: {},
      maxlength: 5,
    },
  },
  isMandatory: false,
  disable: false,
  component: undefined,
  config: {
    inline: true,
    label: "Label",
    isMandatory: false,
    type: "text",
    disable: false,
    populators: {
      name: "label",
      error: "Required",
      validation: {
        pattern: {},
        maxlength: 5,
      },
    },
  },
  sectionFormCategory: undefined,
  formData: {},
  selectedFormCategory: undefined,
  props: {
    label: "Submit Bar",
    config: [
      {
        head: "Heading",
        body: [
          {
            inline: true,
            label: "Label",
            isMandatory: false,
            type: "text",
            disable: false,
            populators: {
              name: "label",
              error: "Required",
              validation: {
                pattern: {},
                maxlength: 5,
              },
            },
          },
        ],
      },
    ],
    defaultValues: {},
    fieldStyle: {
      marginRight: 0,
    },
  },
  errors: {},
};
