import React from "react";
import { CheckPoint } from "../ConnectingCheckPoints";

export default {
  title: "Atoms/CheckPoint",
  component: CheckPoint,
};

const Template = (args) => <CheckPoint {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  keyValue: 4,
  isCompleted: false,
  label: "Pending for DSO Assignment",
  key: 0,
  info: "",
};

export const Primary = Template.bind({});
Primary.args = {
  keyValue: 4,
  isCompleted: false,
  label: "Pending for DSO Assignment",
  key: 0,
  info: "",
};


