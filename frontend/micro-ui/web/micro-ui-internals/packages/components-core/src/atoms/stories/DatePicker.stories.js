import React, { Children } from "react";
import DatePicker from "../DatePicker";

export default {
  title: "Atoms/DatePicker",
  component: DatePicker,
};

const Template = (args) => <DatePicker {...args} />;

export const PlayGround = Template.bind({});
PlayGround.args = {
  className: "",
  style: {},
  date: "",
  min: "",
  max: "",
  isRequired: false,
  onChange: () => {
    console.log("Change");
  },
  onBlur: () => {
    console.log("Blur");
  },
};

export const Primary = Template.bind({});
Primary.args = {};
