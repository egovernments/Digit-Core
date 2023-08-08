import React from "react";
import CitizenInfoLabel from "../CitizenInfoLabel";

export default {
  title: "Atoms/CitizenInfoLabel",
  component: CitizenInfoLabel,
  argTypes: {
    text: {
      control: { type: "text" },
    },
    info: {
      control: { type: "text" },
    },
    fill: {
      control: { type: "color" },
    },
    textStyle: {
      control: { type: "object" },
    },
    className: {
      control: { type: "text" },
    },
    style: {
      control: { type: "object" },
    },
    showInfo: {
      control: { type: "boolean" },
    },
  },
};

const Template = (args) => <CitizenInfoLabel {...args} />;

export const Default = Template.bind({});
Default.args = {
  text: "Main content text",
  info: "Info banner text",
};
