import React from "react";

import ActionLinks from "../ActionLinks";

export default {
  title: "Atom/ActionLinks",
  component: ActionLinks,
};

const Template = (args) => <ActionLinks {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" },
  children: <span>This is action link.</span>,
};

export const Primary = Template.bind({});
Primary.args = {
  children: <span>This is a action link.</span>,
};
