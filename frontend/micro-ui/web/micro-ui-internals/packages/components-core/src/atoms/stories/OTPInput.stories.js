import React from "react";
import { action } from "@storybook/addon-actions";
import OTPInput from "../OTPInput";

export default {
  title: "Atoms/OTPInput",
  component: OTPInput,
};

const Template = (args) => <OTPInput {...args} />;

const defaultProps = {
  length: 6,
  value: "",
  onChange: action("onChange"),
};

export const Default = Template.bind({});
Default.args = {
  ...defaultProps,
};

// Create a Story for the OTPInput component with a value and length
export const WithValue = Template.bind({});
WithValue.args = {
  ...defaultProps,
  value: "123456",
};

// Create a Story for the OTPInput component with a different length
export const CustomLength = Template.bind({});
CustomLength.args = {
  ...defaultProps,
  length: 4,
};

// Create a Story for the OTPInput component with a focused input
export const FocusedInput = Template.bind({});
FocusedInput.args = {
  ...defaultProps,
  value: "1",
  isFocus: true,
};

// Create a Story for the OTPInput component with disabled inputs
export const DisabledInputs = Template.bind({});
DisabledInputs.args = {
  ...defaultProps,
  value: "123456",
  disable: true,
};

// Create a Story for the OTPInput component with error styling
export const ErrorStyle = Template.bind({});
ErrorStyle.args = {
  ...defaultProps,
  value: "12",
  errorStyle: true,
};

// Create a Story for the OTPInput component with a custom className and style
export const CustomStyle = Template.bind({});
CustomStyle.args = {
  ...defaultProps,
  value: "123456",
  className: "custom-otp-input",
  style: { color: "red", fontWeight: "bold" },
};
