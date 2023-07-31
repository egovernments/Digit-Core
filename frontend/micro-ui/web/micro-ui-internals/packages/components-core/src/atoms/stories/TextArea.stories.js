import React from "react";
import TextArea from "../TextArea";

export default {
  title: "Atoms/TextArea",
  component: TextArea,
};

const Template = (args) => <TextArea {...args} />;

// Default TextArea with placeholder
export const Default = Template.bind({});
Default.args = {
  name: "textArea",
  placeholder: "Enter your text here",
};

// TextArea with an initial value
export const WithValue = Template.bind({});
WithValue.args = {
  name: "textArea",
  value: "Some text",
};

// Disabled TextArea
export const Disabled = Template.bind({});
Disabled.args = {
  name: "textArea",
  disabled: true,
};

// Custom style applied to TextArea
export const CustomStyle = Template.bind({});
CustomStyle.args = {
  name: "textArea",
  style: { border: "1px solid red", borderRadius: "5px", padding: "8px" },
};
