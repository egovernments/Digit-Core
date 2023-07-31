import React from "react";
import { Accessible } from "./Accessible";

export default {
  tags: ["autodocs"],
  argTypes: {
    className: {
      options: ["custom-class"],
      control: { type: "check" },
    },
  },
  title: "Accessible",
  component: Accessible,
};

export const Default = () => <Accessible />;
export const Fill = () => <Accessible fill="blue" />;
export const Size = () => <Accessible height="50" width="50" />;
export const CustomStyle = () => <Accessible style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Accessible className="custom-class" />;
export const Clickable = () => <Accessible onClick={() => console.log("clicked")} />;

const Template = (args) => <Accessible {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" },
};
