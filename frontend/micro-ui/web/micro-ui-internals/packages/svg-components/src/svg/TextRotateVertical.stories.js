import React from "react";
import { TextRotateVertical } from "./TextRotateVertical";

export default {
  tags: ["autodocs"],
  argTypes: {
    className: {
      options: ["custom-class"],
      control: { type: "check" },
    },
  },
  title: "TextRotateVertical",
  component: TextRotateVertical,
};

export const Default = () => <TextRotateVertical />;
export const Fill = () => <TextRotateVertical fill="blue" />;
export const Size = () => <TextRotateVertical height="50" width="50" />;
export const CustomStyle = () => <TextRotateVertical style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TextRotateVertical className="custom-class" />;
export const Clickable = () => <TextRotateVertical onClick={() => console.log("clicked")} />;

const Template = (args) => <TextRotateVertical {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" },
};
