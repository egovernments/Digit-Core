import React, { Children } from "react";
import Dropdown from "../Dropdown";

export default {
  title: "Atom/Dropdown",
  component: Dropdown,
};

const Template = (args) => <Dropdown {...args} />;

export const PlayGround = Template.bind({});
PlayGround.args = {};

export const Primary = Template.bind({});
Primary.args = {};
