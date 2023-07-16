import React from "react";
import { AccountBox } from "./AccountBox";

export default {
  tags: ["autodocs"],
  argTypes: {
    className: {
      options: ["custom-class"],
      control: { type: "check" },
    },
  },
  title: "AccountBox",
  component: AccountBox,
};

export const Default = () => <AccountBox />;
export const Fill = () => <AccountBox fill="blue" />;
export const Size = () => <AccountBox height="50" width="50" />;
export const CustomStyle = () => <AccountBox style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AccountBox className="custom-class" />;
export const Clickable = () => <AccountBox onClick={() => console.log("clicked")} />;

const Template = (args) => <AccountBox {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" },
};
