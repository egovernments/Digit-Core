import React from "react";
import { QueryClient, QueryClientProvider } from "react-query";
import CustomDropdown from "../CustomDropdown";

export default {
  title: "Molecules/CustomDropdown",
  component: CustomDropdown,
  argTypes: {
    t: { control: false },
    config: { control: "object" },
    inputRef: { control: false },
    label: { control: "text" },
    onChange: { action: "onChange" },
    value: { control: "text" },
    errorStyle: { control: "object" },
    disable: { control: "boolean" },
    type: { control: "radio", options: ["radio", "dropdown"] },
    additionalWrapperClass: { control: "text" },
    props: { control: "object" },
  },
};
const queryClient = new QueryClient();

const Template = (args) => (
  <QueryClientProvider client={queryClient}>
    <CustomDropdown {...args} />
  </QueryClientProvider>
);

// Mocking t function for translations
const t = (key) => key;

//mock options data
const gendersOptions = [
  { code: "MALE", name: "MALE" },
  { code: "FEMALE", name: "FEMALE" },
  { code: "TRANSGENDER", name: "TRANSGENDER" },
];

// Story for CustomDropdown with radio type
export const CustomDropdownRadioStory = Template.bind({});
CustomDropdownRadioStory.args = {
  t: t,
  config: {
    name: "gender",
    optionsKey: "name",
    styles: { display: "flex", justifyContent: "flex-start", gap: "3rem" },
    options: gendersOptions,
  },
  inputRef: null,
  label: "Enter Gender",
  onChange: (e, name) => console.log("Selected value:", e, "Name:", name),
  value: "MALE",
  errorStyle: null,
  disable: false,
  type: "radio",
  additionalWrapperClass: "",
};

// Story for CustomDropdown with dropdown type
export const CustomDropdownDropdownStory = Template.bind({});
CustomDropdownDropdownStory.args = {
  t,
  config: {
    name: "genders",

    styles: { display: "flex", justifyContent: "space-between" },
    defaultValue: "FEMALE",
    optionsCustomStyle: { color: "blue", fontSize: "14px" },
    optionsKey: "name",
    options: gendersOptions,
  },
  inputRef: null,
  label: "Enter Gender",
  onChange: (e, name) => console.log("Selected value:", e, "Name:", name),
  errorStyle: null,
  disable: false,
  type: "dropdown",
  additionalWrapperClass: "",
  props: {
    isLoading: false,
    data: gendersOptions,
  },
};
