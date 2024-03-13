import React from "react";
import { Accessibility } from "./Accessibility";

export default {
  tags: ["autodocs"],
  argTypes: {
    className: {
      options: ["custom-class"],
      control: { type: "check" },
    },
  },
  title: "Accessibility",
  component: Accessibility,
};

const Template = (args) => <Accessibility {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" },
};

export const Default = () => <Accessibility />;
export const Fill = () => <Accessibility fill="blue" />;
export const Size = () => <Accessibility height="50" width="50" />;
export const CustomStyle = () => <Accessibility style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Accessibility className="custom-class" />;
export const Clickable = () => <Accessibility onClick={() => console.log("clicked")} />;

