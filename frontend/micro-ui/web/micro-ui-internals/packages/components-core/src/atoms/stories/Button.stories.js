import React from "react";
import Button from "../Button";

export default {
  title: "Atoms/Button",
  component: Button,
  argTypes: {
    isDisabled: {
      control: "boolean",
    },
    label: {
      control: "text",
    },
    variation: {
      control: "text",
    },
    className: {
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

const Template = (args) => <Button {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  label: "Play-Ground",
  className: "custom-class",
  style: {},
  onClick: () => console.log("clicked"),
};

export const Primary = Template.bind({});
Primary.args = {
  variation: "primary",
  label: "Primary",
  style: {},
  onClick: () => console.log("Primary clicked"),
};

export const secondary = Template.bind({});
secondary.args = {
  variation: "secondary",
  label: "Secondary",
  style: {},
  onClick: () => console.log("Secondary clicked"),
};
