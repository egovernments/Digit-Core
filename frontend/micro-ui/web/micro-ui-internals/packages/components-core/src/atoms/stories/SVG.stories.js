import React from "react";
import { SVG } from "../SVG";

export default {
  tags: ["autodocs"],
  argTypes: {
    className: {
      options: ["custom-class"],
      control: { type: "check" },
    },
  },
  title: "SVG",
  component: SVG,
};

const Template = (args) => <SVG.Accessibility {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" },
  fill: "blue",
  height: "50",
  width: "50",
  onClick:()=>{}
};
