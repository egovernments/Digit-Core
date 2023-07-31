import React from "react";
import { action } from "@storybook/addon-actions";
import MobileNumber from "../MobileNumber";

export default {
  title: "Atoms/MobileNumber",
  component: MobileNumber,
};

const Template = (args) => <MobileNumber {...args} />;

export const Default = Template.bind({});
Default.args = {
  placeholder: "Enter mobile number",
  onChange: action("Mobile number changed"),
};

export const WithValue = Template.bind({});
WithValue.args = {
  value: "1234567890",
  placeholder: "Enter mobile number",
  onChange: action("Mobile number changed"),
};

export const WithMaxLength = Template.bind({});
WithMaxLength.args = {
  maxLength: 12,
  placeholder: "Enter mobile number",
  onChange: action("Mobile number changed"),
};

export const WithDisabledState = Template.bind({});
WithDisabledState.args = {
  value: "9876543210",
  placeholder: "Enter mobile number",
  onChange: action("Mobile number changed"),
  disable: true,
};

export const WithErrorStyle = Template.bind({});
WithErrorStyle.args = {
  value: "12345",
  placeholder: "Enter mobile number",
  onChange: action("Mobile number changed"),
  errorStyle: true,
};

export const WithCustomStyle = Template.bind({});
WithCustomStyle.args = {
  value: "9876543210",
  placeholder: "Enter mobile number",
  onChange: action("Mobile number changed"),
  style: { border: "2px solid red", borderRadius: "5px", padding: "8px" },
};

export const WithCustomClass = Template.bind({});
WithCustomClass.args = {
  value: "1234567890",
  placeholder: "Enter mobile number",
  onChange: action("Mobile number changed"),
  className: "custom-class",
};

export const WithHiddenSpan = Template.bind({});
WithHiddenSpan.args = {
  value: "9876543210",
  placeholder: "Enter mobile number",
  onChange: action("Mobile number changed"),
  hideSpan: true,
};
