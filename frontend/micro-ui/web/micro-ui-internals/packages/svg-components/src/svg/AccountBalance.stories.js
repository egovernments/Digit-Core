import React from "react";
import { AccountBalance } from "./AccountBalance";

export default {
  tags: ["autodocs"],
  argTypes: {
    className: {
      options: ["custom-class"],
      control: { type: "check" },
    },
  },
  title: "AccountBalance",
  component: AccountBalance,
};

export const Default = () => <AccountBalance />;
export const Fill = () => <AccountBalance fill="blue" />;
export const Size = () => <AccountBalance height="50" width="50" />;
export const CustomStyle = () => <AccountBalance style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AccountBalance className="custom-class" />;
export const Clickable = () => <AccountBalance onClick={() => console.log("clicked")} />;

const Template = (args) => <AccountBalance {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" },
};
