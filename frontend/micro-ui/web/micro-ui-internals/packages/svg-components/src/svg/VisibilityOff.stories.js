import React from "react";
import { VisibilityOff } from "./VisibilityOff";

export default {
  title: "VisibilityOff",
  component: VisibilityOff,
};

export const Default = () => <VisibilityOff />;
export const Fill = () => <VisibilityOff fill="blue" />;
export const Size = () => <VisibilityOff height="50" width="50" />;
export const CustomStyle = () => <VisibilityOff style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <VisibilityOff className="custom-class" />;
