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
          "mobileNumber",
          // "paragraph",
          // "custom",
          // "checkbox",
          // "multiupload",
          // "select",
          // "radio",
          // "dropdown",
          // "radioordropdown",
          // "component",
          // "documentUpload",
          // "form",
          // "locationdropdown",
          // "apidropdown",
          // "multiselectdropdown",
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

const Template = (args) => {
  return <FieldComposer {...args} />;
};

export const Playground = Template.bind({});
Playground.args = {
  type: "date",
  variant: "default",
  populators: {
    name: "Label",
    error: "Required",
    validation: {
      pattern: {},
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
