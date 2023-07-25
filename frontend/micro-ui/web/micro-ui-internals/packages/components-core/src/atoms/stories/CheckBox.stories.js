import React, { useState } from "react";
import CheckBox from "../CheckBox";

export default {
  title: "Atom/CheckBox",
  component: CheckBox,
};

const Template = (args) => <CheckBox {...args} />;

export const Primary = Template.bind({});

export const Checked = Template.bind({});
Checked.args = {
  checked: true,
};

export const PlayGround = Template.bind({});
PlayGround.args = {
  checked: true,
  onChange: () => {
    console.log("clicked");
  },
};
