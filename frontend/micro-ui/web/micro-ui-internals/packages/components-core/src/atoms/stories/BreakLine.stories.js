import React from "react";

import BreakLine from "../BreakLine";

export default {
  title: "Atoms/BreakLine",
  component: BreakLine,
  argTypes: {
    className: {
      control: "text",
    },
    style: {
      control: { type: "object" },
    },
  },
};

const Template = (args) => <BreakLine {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" },
};

export const Primary = Template.bind({});