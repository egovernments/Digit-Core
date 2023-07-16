import React from "react";
import { AccountBalance } from "./AccountBalance";

export default {
  title: "AccountBalance",
  component: AccountBalance,
};

export const Default = () => <AccountBalance />;
export const Fill = () => <AccountBalance fill="blue" />;
export const Size = () => <AccountBalance height="50" width="50" />;
export const CustomStyle = () => <AccountBalance style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AccountBalance className="custom-class" />;
