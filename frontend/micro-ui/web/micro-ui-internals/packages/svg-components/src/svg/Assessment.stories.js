import React from "react";
import { Assessment } from "./Assessment";

export default {
  title: "Assessment",
  component: Assessment,
};

export const Default = () => <Assessment />;
export const Fill = () => <Assessment fill="blue" />;
export const Size = () => <Assessment height="50" width="50" />;
export const CustomStyle = () => <Assessment style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Assessment className="custom-class" />;
