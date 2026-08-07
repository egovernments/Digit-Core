import React from "react";
import { StarEmpty } from "./StarEmpty"; // Adjust the import path as per your project structure

export default {
  tags: ["autodocs"],
  argTypes: {
    className: {
      options: ["custom-class"],
      control: { type: "check" },
    },
  },
  title: "StarEmpty",
  component: StarEmpty,
};

const Template = (args) => <StarEmpty {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" },
};

export const Default = () => <StarEmpty />;
export const Fill = () => <StarEmpty fill="blue" />;
export const Size = () => <StarEmpty height="50" width="50" />;
export const CustomStyle = () => <StarEmpty style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <StarEmpty className="custom-class" />;
export const Clickable = () => <StarEmpty onClick={() => console.log("clicked")} />;

