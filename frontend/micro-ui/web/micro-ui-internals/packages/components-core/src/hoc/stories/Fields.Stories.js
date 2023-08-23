import React from "react";
import Fields from "../Fields";

export default {
  title: "Atom-Groups/Fields",
  component: Fields,
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
  },
};

const Template = (args) => <Fields type={args?.type} {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  type: args?.type,
  populators: {
    name: "salutation",
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
    label: "Salutation",
    isMandatory: false,
    type: "text",
    disable: false,
    populators: {
      name: "salutation",
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
        head: "Heading 1",
        body: [
          {
            inline: true,
            label: "Salutation",
            isMandatory: false,
            type: "text",
            disable: false,
            populators: {
              name: "salutation",
              error: "Required",
              validation: {
                pattern: {},
                maxlength: 5,
              },
            },
          },
          {
            inline: true,
            label: "Name",
            isMandatory: true,
            type: "text",
            disable: false,
            populators: {
              name: "name",
              error: "Required",
              validation: {
                pattern: {},
              },
            },
          },
          {
            isMandatory: true,
            type: "radio",
            key: "genders",
            label: "Gender",
            disable: false,
            populators: {
              name: "gender",
              optionsKey: "name",
              error: "sample required message",
              required: true,
              mdmsConfig: {
                masterName: "GenderType",
                moduleName: "common-masters",
                localePrefix: "COMMON_GENDER",
              },
            },
          },
          {
            label: "Age",
            isMandatory: true,
            type: "number",
            disable: false,
            populators: {
              name: "age",
              error: "sample error message",
              validation: {
                min: 0,
                max: 100,
              },
            },
          },
          {
            inline: true,
            label: "DOB",
            isMandatory: true,
            description: "Please enter a valid Date of birth",
            type: "date",
            disable: false,
            populators: {
              name: "dob",
              error: "Required",
              validation: {
                required: true,
              },
            },
          },
          {
            label: "Phone number",
            isMandatory: true,
            type: "mobileNumber",
            disable: false,
            populators: {
              name: "phNumber",
              error: "sample error message",
              validation: {
                min: 5999999999,
                max: 9999999999,
              },
            },
          },
          {
            label: "COMMON_WARD",
            type: "locationdropdown",
            isMandatory: false,
            disable: false,
            populators: {
              name: "ward",
              type: "ward",
              optionsKey: "i18nKey",
              defaultText: "COMMON_SELECT_WARD",
              selectedText: "COMMON_SELECTED",
              allowMultiSelect: false,
            },
          },
          {
            inline: true,
            label: "Address",
            isMandatory: false,
            description: "address details",
            type: "textarea",
            disable: false,
            populators: {
              name: "address",
              error: "Required",
              validation: {
                pattern: {},
              },
            },
          },
          {
            inline: true,
            label: "Enter Random Amount",
            isMandatory: false,
            key: "amountInRs",
            type: "amount",
            disable: false,
            preProcess: {
              convertStringToRegEx: ["populators.validation.pattern"],
            },
            populators: {
              prefix: "₹ ",
              name: "amountInRs",
              error: "PROJECT_PATTERN_ERR_MSG_PROJECT_ESTIMATED_COST",
              validation: {
                pattern: "^[1-9]\\d*(\\.\\d+)?$",
                maxlength: 16,
                step: "0.01",
                min: 0,
                max: 5000000,
              },
            },
          },
        ],
      },
      {
        head: "Heading 2",
        body: [
          {
            label: "Additional Details",
            isMandatory: true,
            description: "Additional Details if any",
            key: "additionalDetails",
            type: "text",
            disable: false,
            populators: {
              name: "additionalDetails",
              error: "sample error message",
              validation: {
                pattern: {},
              },
            },
          },
          {
            isMandatory: false,
            key: "priority",
            type: "radio",
            label: "Enter Priority of Application",
            disable: false,
            populators: {
              name: "priority",
              optionsKey: "name",
              error: "sample required message",
              required: false,
              options: [
                {
                  code: "1",
                  name: "P1",
                },
                {
                  code: "2",
                  name: "P2",
                },
                {
                  code: "3",
                  name: "P3",
                },
              ],
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

export const HeadingXL = () => <div className={`typography text-heading-xl`}>Heading XL</div>;
export const HeadingL = () => <div className="typography text-heading-l">Heading L</div>;
export const HeadingM = () => <div className="typography text-heading-m">Heading M</div>;
export const HeadingS = () => <div className="typography text-heading-s">Heading S</div>;
export const HeadingXS = () => <div className="typography text-heading-xs">Heading XS</div>;
export const CaptionL = () => <div className="typography text-caption-l">Caption L</div>;
export const CaptionM = () => <div className="typography text-caption-m">Caption M</div>;
export const CaptionS = () => <div className="typography text-caption-s">Caption S</div>;
export const BodyL = () => <div className="typography text-body-l">Body L</div>;
export const BodyS = () => <div className="typography text-body-s">Body S</div>;
export const BodyXS = () => <div className="typography text-body-xs">Body XS</div>;
export const Label = () => <div className="typography text-label">Label</div>;
export const Link = () => <div className="typography text-link">Link</div>;
