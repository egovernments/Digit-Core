import React from "react";
import { Accessibility } from "./Accessibility";

export default {
  title: "Accessibility",
  component: Accessibility,
};

export const Default = () => <Accessibility />;
export const Fill = () => <Accessibility fill="blue" />;
export const Size = () => <Accessibility height="50" width="50" />;
export const CustomStyle = () => <Accessibility style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Accessibility className="custom-class" />;
