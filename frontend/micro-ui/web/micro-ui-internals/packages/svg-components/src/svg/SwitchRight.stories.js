import React from "react";
import { SwitchRight } from "./SwitchRight";

export default {
  title: "SwitchRight",
  component: SwitchRight,
};

export const Default = () => <SwitchRight />;
export const Fill = () => <SwitchRight fill="blue" />;
export const Size = () => <SwitchRight height="50" width="50" />;
export const CustomStyle = () => <SwitchRight style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SwitchRight className="custom-class" />;
