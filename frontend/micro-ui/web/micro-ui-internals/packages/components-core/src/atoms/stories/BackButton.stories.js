import React from "react";
import BackButton from "../BackButton";

export default {
  title: "Atoms/BackButton",
  component: BackButton,
  argTypes: {
    className: {
      control: "text",
    },
    variant: {
      control: "text",
    },
    style: {
      control: { type: "object" },
    },
    onClick: {
      control: "function",
    },
  },
};

const Template = (args) => <BackButton {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { "font-size": "20px" },
  onClick: () => console.log("clicked"),
};

export const Primary = Template.bind({});
