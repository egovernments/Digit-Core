import React from "react";
import Amount from "../Amount";

export default {
  title: "Atoms/Amount",
  component: Amount,
  argTypes: {
    className: {
      control: "text",
    },
    style: {
      control: { type: "object" },
    },
    roundOff: {
      control: { type: "boolean" },
    },
    value: {
      control: { type: "number" },
    },
  },
};

const Template = (args) => <Amount {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { color: "green" },
  roundOff: true,
  value: 550010,
};

export const Primary = Template.bind({});
Playground.args = {
  value: 55000,
};
