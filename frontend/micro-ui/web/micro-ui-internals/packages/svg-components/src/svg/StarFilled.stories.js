import React from "react";
import { StarFilled } from "./StarFilled"; // Adjust the import path as per your project structure

export default {
  tags: ["autodocs"],
  argTypes: {
    className: {
      options: ["custom-class"],
      control: { type: "check" },
    },
  },
  title: "StarFilled",
  component: StarFilled,
};

export const Fill = () => <StarFilled fill="blue" />;
export const Size = () => <StarFilled height="50" width="50" />;
export const CustomStyle = () => <StarFilled style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <StarFilled className="custom-class" />;
export const Clickable = () => <StarFilled onClick={() => console.log("clicked")} />;

const Template = (args) => <StarFilled {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" },
  fill: "green",
};
