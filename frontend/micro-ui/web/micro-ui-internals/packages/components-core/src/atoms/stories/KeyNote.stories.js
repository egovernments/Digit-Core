import React, { Children } from "react";
import KeyNote from "../KeyNote";

export default {
  title: "Atoms/KeyNote",
  component: KeyNote,
};

const Template = (args) => <KeyNote {...args} />;

export const PlayGround = Template.bind({});
PlayGround.args = {
  keyValue: "MAX",
  note: 7867868,
};

export const Primary = Template.bind({});
Primary.args = {
  keyValue: "MAX",
  note: 7867868,
};
