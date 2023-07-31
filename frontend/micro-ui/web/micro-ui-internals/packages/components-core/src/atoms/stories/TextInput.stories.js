import React from "react";
import { action } from "@storybook/addon-actions";
import TextInput from "../TextInput";

export default {
  title: "Atoms/TextInput",
  component: TextInput,
};

const Template = (args) => <TextInput {...args} />;

export const Default = Template.bind({});
Default.args = {
  placeholder: "Enter text",
  onChange: action("Text changed"),
};

export const WithValue = Template.bind({});
WithValue.args = {
  value: "Example text",
  placeholder: "Enter text",
  onChange: action("Text changed"),
};

export const WithMaxLength = Template.bind({});
WithMaxLength.args = {
  maxLength: 50,
  placeholder: "Enter text",
  onChange: action("Text changed"),
};

export const WithDisabledState = Template.bind({});
WithDisabledState.args = {
  value: "Disabled",
  placeholder: "Enter text",
  onChange: action("Text changed"),
  disable: true,
};

export const WithErrorStyle = Template.bind({});
WithErrorStyle.args = {
  value: "Error",
  placeholder: "Enter text",
  onChange: action("Text changed"),
  errorStyle: true,
};

export const WithCustomStyle = Template.bind({});
WithCustomStyle.args = {
  value: "Custom style",
  placeholder: "Enter text",
  onChange: action("Text changed"),
  style: { border: "2px solid red", borderRadius: "5px", padding: "8px" },
};

export const WithCustomClass = Template.bind({});
WithCustomClass.args = {
  value: "Custom class",
  placeholder: "Enter text",
  onChange: action("Text changed"),
  className: "custom-class",
};
