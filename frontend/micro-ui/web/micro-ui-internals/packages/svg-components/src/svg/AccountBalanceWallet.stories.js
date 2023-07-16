import React from "react";
import { AccountBalanceWallet } from "./AccountBalanceWallet";

export default {
  title: "AccountBalanceWallet",
  component: AccountBalanceWallet,
};

export const Default = () => <AccountBalanceWallet />;
export const Fill = () => <AccountBalanceWallet fill="blue" />;
export const Size = () => <AccountBalanceWallet height="50" width="50" />;
export const CustomStyle = () => <AccountBalanceWallet style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AccountBalanceWallet className="custom-class" />;
