import React from "react";
import { action } from "@storybook/addon-actions";
import InputTextAmount from "../InputTextAmount";
export default {
  title: "Atoms/InputTextAmount",
  component: InputTextAmount,
};

const Template = (args) => <InputTextAmount {...args} />;

export const Default = Template.bind({});
Default.args = {
  value: "",
  prefix: "₹ ",
  onChange: action("Amount changed"),
};

export const WithValue = Template.bind({});
WithValue.args = {
  value: "1000.5",
  prefix: "₹ ",
  onChange: action("Amount changed"),
};

export const WithDisabledState = Template.bind({});
WithDisabledState.args = {
  value: "500.75",
  prefix: "₹ ",
  onChange: action("Amount changed"),
  disabled: true,
};

export const WithCustomStyle = Template.bind({});
WithCustomStyle.args = {
  value: "2500",
  prefix: "₹ ",
  onChange: action("Amount changed"),
  style: { color: "green", fontSize: "18px", fontWeight: "bold" },
};

export const WithCustomDecimalScale = Template.bind({});
WithCustomDecimalScale.args = {
  value: "1234.5678",
  prefix: "₹ ",
  onChange: action("Amount changed"),
  decimalScale: 3,
};

export const WithCustomStep = Template.bind({});
WithCustomStep.args = {
  value: "0",
  prefix: "₹ ",
  onChange: action("Amount changed"),
  step: 100,
};
