import React from "react";
import { AccountBox } from "./AccountBox";

export default {
  title: "AccountBox",
  component: AccountBox,
};

export const Default = () => <AccountBox />;
export const Fill = () => <AccountBox fill="blue" />;
export const Size = () => <AccountBox height="50" width="50" />;
export const CustomStyle = () => <AccountBox style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AccountBox className="custom-class" />;
