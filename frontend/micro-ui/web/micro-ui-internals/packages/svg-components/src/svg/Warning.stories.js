import React from "react";
import { Warning } from "./Warning";

export default {
  title: "Warning",
  component: Warning,
};

export const Default = () => <Warning />;
export const Fill = () => <Warning fill="blue" />;
export const Size = () => <Warning height="50" width="50" />;
export const CustomStyle = () => <Warning style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Warning className="custom-class" />;
