import React from "react";
import { Android } from "./Android";

export default {
  title: "Android",
  component: Android,
};

export const Default = () => <Android />;
export const Fill = () => <Android fill="blue" />;
export const Size = () => <Android height="50" width="50" />;
export const CustomStyle = () => <Android style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Android className="custom-class" />;
