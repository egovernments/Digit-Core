import React from "react";
import { AccountBalanceWallet } from "./AccountBalanceWallet";

export default {
  tags: ["autodocs"],
  argTypes: {
    className: {
      options: ["custom-class"],
      control: { type: "check" },
    },
  },
  title: "AccountBalanceWallet",
  component: AccountBalanceWallet,
};

export const Default = () => <AccountBalanceWallet />;
export const Fill = () => <AccountBalanceWallet fill="blue" />;
export const Size = () => <AccountBalanceWallet height="50" width="50" />;
export const CustomStyle = () => <AccountBalanceWallet style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AccountBalanceWallet className="custom-class" />;
export const Clickable = () => <AccountBalanceWallet onClick={() => console.log("clicked")} />;

const Template = (args) => <AccountBalanceWallet {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" },
};
