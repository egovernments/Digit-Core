import React from "react";
import { SouthWest } from "./SouthWest";

export default {
  title: "SouthWest",
  component: SouthWest,
};

export const Default = () => <SouthWest />;
export const Fill = () => <SouthWest fill="blue" />;
export const Size = () => <SouthWest height="50" width="50" />;
export const CustomStyle = () => <SouthWest style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SouthWest className="custom-class" />;
