import React from "react";
import { AccessibilityNew } from "./AccessibilityNew";

export default {
  tags: ["autodocs"],
  argTypes: {
    className: {
      options: ["custom-class"],
      control: { type: "check" },
    },
  },
  title: "AccessibilityNew",
  component: AccessibilityNew,
};

export const Default = () => <AccessibilityNew />;
export const Fill = () => <AccessibilityNew fill="blue" />;
export const Size = () => <AccessibilityNew height="50" width="50" />;
export const CustomStyle = () => <AccessibilityNew style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AccessibilityNew className="custom-class" />;
export const Clickable = () => <AccessibilityNew onClick={() => console.log("clicked")} />;

const Template = (args) => <AccessibilityNew {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" },
};
