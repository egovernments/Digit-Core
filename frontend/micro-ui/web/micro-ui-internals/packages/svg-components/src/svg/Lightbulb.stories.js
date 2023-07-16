import React from "react";
import { Lightbulb } from "./Lightbulb";

export default {
  title: "Lightbulb",
  component: Lightbulb,
};

export const Default = () => <Lightbulb />;
export const Fill = () => <Lightbulb fill="blue" />;
export const Size = () => <Lightbulb height="50" width="50" />;
export const CustomStyle = () => <Lightbulb style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Lightbulb className="custom-class" />;
