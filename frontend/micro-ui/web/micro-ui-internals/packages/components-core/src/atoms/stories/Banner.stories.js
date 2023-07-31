import React from "react";
import Banner from "../Banner";

export default {
  title: "Atoms/Banner",
  component: Banner,
  argTypes: {},
};

const Template = (args) => <Banner {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  successful: true,
  message: "Application Submitted",
  whichSvg: "tick",
  complaintNumber: "20230725-001",
};

export const Primary = Template.bind({});
Primary.args = {};
