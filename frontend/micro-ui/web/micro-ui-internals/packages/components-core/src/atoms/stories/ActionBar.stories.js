import React from "react";

import ActionBar from "../ActionBar";

export default {
  title: "Atom/ActionBar",
  component: ActionBar,
  argTypes: {
    className: {
      control: "text",
    },
    style: {
      control: { type: "object" },
    },
    children: {
      control: { type: "object" },
    },
  },
};

const Template = (args) => <ActionBar {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" },
  children: (
    <span>
      <p>COMMON_SELECTED</p>
    </span>
  ),
};

export const Primary = Template.bind({});
Primary.args = {
  children: <p>Action Bar</p>,
};
