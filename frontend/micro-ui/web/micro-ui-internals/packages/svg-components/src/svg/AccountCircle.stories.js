import React from "react";
import { AccountCircle } from "./AccountCircle";

export default {
  tags: ["autodocs"],
  argTypes: {
    className: {
      options: ["custom-class"],
      control: { type: "check" },
    },
  },
  title: "AccountCircle",
  component: AccountCircle,
};

export const Default = () => <AccountCircle />;
export const Fill = () => <AccountCircle fill="blue" />;
export const Size = () => <AccountCircle height="50" width="50" />;
export const CustomStyle = () => <AccountCircle style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AccountCircle className="custom-class" />;
export const Clickable = () => <AccountCircle onClick={() => console.log("clicked")} />;

const Template = (args) => <AccountCircle {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" },
};
