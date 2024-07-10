import React from "react";
import { AccessibleForward } from "./AccessibleForward";

export default {
  tags: ["autodocs"],
  argTypes: {
    className: {
      options: ["custom-class"],
      control: { type: "check" },
    },
  },
  title: "AccessibleForward",
  component: AccessibleForward,
};

export const Default = () => <AccessibleForward />;
export const Fill = () => <AccessibleForward fill="blue" />;
export const Size = () => <AccessibleForward height="50" width="50" />;
export const CustomStyle = () => <AccessibleForward style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AccessibleForward className="custom-class" />;
export const Clickable = () => <AccessibleForward onClick={() => console.log("clicked")} />;

const Template = (args) => <AccessibleForward {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" },
};
