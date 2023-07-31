import React, { Children } from "react";
import ErrorMessage from "../ErrorMessage";

export default {
  title: "Atoms/ErrorMessage",
  component: ErrorMessage,
};

const Template = (args) => <ErrorMessage {...args} />;

export const PlayGround = Template.bind({});
PlayGround.args = {
  message: "Invalid play ground screen"
};

export const Primary = Template.bind({});
Primary.args = {
  message: "Invalid data format"
};
