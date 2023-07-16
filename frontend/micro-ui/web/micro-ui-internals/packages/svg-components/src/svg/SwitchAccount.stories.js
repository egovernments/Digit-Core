import React from "react";
import { SwitchAccount } from "./SwitchAccount";

export default {
  title: "SwitchAccount",
  component: SwitchAccount,
};

export const Default = () => <SwitchAccount />;
export const Fill = () => <SwitchAccount fill="blue" />;
export const Size = () => <SwitchAccount height="50" width="50" />;
export const CustomStyle = () => <SwitchAccount style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SwitchAccount className="custom-class" />;
