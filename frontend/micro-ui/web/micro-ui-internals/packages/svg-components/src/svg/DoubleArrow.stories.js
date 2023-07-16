import React from "react";
import { DoubleArrow } from "./DoubleArrow";

export default {
  title: "DoubleArrow",
  component: DoubleArrow,
};

export const Default = () => <DoubleArrow />;
export const Fill = () => <DoubleArrow fill="blue" />;
export const Size = () => <DoubleArrow height="50" width="50" />;
export const CustomStyle = () => <DoubleArrow style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <DoubleArrow className="custom-class" />;
