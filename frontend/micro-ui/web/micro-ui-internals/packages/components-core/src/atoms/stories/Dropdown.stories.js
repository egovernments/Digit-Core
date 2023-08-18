import React, { Children } from "react";
import Dropdown from "../Dropdown";

export default {
  title: "Atoms/Dropdown",
  component: Dropdown,
};

const options = [
  { id: 1, name: "Option 1" },
  { id: 2, name: "Option 2" },
  { id: 3, name: "Option 3" },
];
const Template = (args) => <Dropdown {...args} />;

export const PlayGround = Template.bind({});
PlayGround.args = {};

export const Primary = Template.bind({});
Primary.args = {
  populators: {},
  formData: {},
  props: {},
  inputRef: null,
  errors: {},
  setValue: () => {},
  option: options,
  optionKey: "name",
};
