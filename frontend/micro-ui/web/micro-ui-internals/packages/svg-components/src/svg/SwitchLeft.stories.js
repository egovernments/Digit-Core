import React from "react";
import { SwitchLeft } from "./SwitchLeft";

export default {
  title: "SwitchLeft",
  component: SwitchLeft,
};

export const Default = () => <SwitchLeft />;
export const Fill = () => <SwitchLeft fill="blue" />;
export const Size = () => <SwitchLeft height="50" width="50" />;
export const CustomStyle = () => <SwitchLeft style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SwitchLeft className="custom-class" />;
