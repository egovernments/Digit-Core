import React from "react";
import { ViewArray } from "./ViewArray";

export default {
  title: "ViewArray",
  component: ViewArray,
};

export const Default = () => <ViewArray />;
export const Fill = () => <ViewArray fill="blue" />;
export const Size = () => <ViewArray height="50" width="50" />;
export const CustomStyle = () => <ViewArray style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ViewArray className="custom-class" />;
