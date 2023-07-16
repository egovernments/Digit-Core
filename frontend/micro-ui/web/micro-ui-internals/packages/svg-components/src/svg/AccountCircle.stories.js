import React from "react";
import { AccountCircle } from "./AccountCircle";

export default {
  title: "AccountCircle",
  component: AccountCircle,
};

export const Default = () => <AccountCircle />;
export const Fill = () => <AccountCircle fill="blue" />;
export const Size = () => <AccountCircle height="50" width="50" />;
export const CustomStyle = () => <AccountCircle style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AccountCircle className="custom-class" />;
