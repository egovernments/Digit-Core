import React from "react";

import AppContainer from "../AppContainer";

export default {
  title: "Atoms/AppContainer",
  component: AppContainer,
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

const Template = (args) => <AppContainer {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" },
  children: (
    <div>
      <h2>App Container</h2>,
    </div>
  ),
};

export const Primary = Template.bind({});
Primary.args = {
  children: (
    <div>
      <h2>App Container</h2>,
    </div>
  ),
};
