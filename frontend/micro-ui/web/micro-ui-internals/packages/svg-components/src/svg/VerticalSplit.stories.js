import React from "react";
import { VerticalSplit } from "./VerticalSplit";

export default {
  title: "VerticalSplit",
  component: VerticalSplit,
};

export const Default = () => <VerticalSplit />;
export const Fill = () => <VerticalSplit fill="blue" />;
export const Size = () => <VerticalSplit height="50" width="50" />;
export const CustomStyle = () => <VerticalSplit style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <VerticalSplit className="custom-class" />;
