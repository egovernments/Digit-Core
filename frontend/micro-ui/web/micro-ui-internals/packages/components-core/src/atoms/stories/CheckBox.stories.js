import React, { useState } from "react";
import CheckBox from "../CheckBox";

export default {
  title: "Atoms/CheckBox",
  component: CheckBox,
};

const Template = (args) => <CheckBox {...args} />;


export const PlayGround = Template.bind({});
PlayGround.args = {
  checked: true,
  onChange: () => {
    console.log("clicked");
  },
};

export const Unchecked = Template.bind({});

export const Checked = Template.bind({});
Checked.args = {
  checked: true,
};
