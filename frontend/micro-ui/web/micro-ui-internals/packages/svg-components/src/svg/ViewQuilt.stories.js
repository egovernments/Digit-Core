import React from "react";
import { ViewQuilt } from "./ViewQuilt";

export default {
  title: "ViewQuilt",
  component: ViewQuilt,
};

export const Default = () => <ViewQuilt />;
export const Fill = () => <ViewQuilt fill="blue" />;
export const Size = () => <ViewQuilt height="50" width="50" />;
export const CustomStyle = () => <ViewQuilt style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ViewQuilt className="custom-class" />;
