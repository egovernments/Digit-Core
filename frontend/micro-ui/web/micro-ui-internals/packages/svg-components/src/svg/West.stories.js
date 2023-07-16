import React from "react";
import { West } from "./West";

export default {
  title: "West",
  component: West,
};

export const Default = () => <West />;
export const Fill = () => <West fill="blue" />;
export const Size = () => <West height="50" width="50" />;
export const CustomStyle = () => <West style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <West className="custom-class" />;
