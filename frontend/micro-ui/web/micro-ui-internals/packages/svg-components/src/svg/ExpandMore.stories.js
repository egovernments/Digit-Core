import React from "react";
import { ExpandMore } from "./ExpandMore";

export default {
  title: "ExpandMore",
  component: ExpandMore,
};

export const Default = () => <ExpandMore />;
export const Fill = () => <ExpandMore fill="blue" />;
export const Size = () => <ExpandMore height="50" width="50" />;
export const CustomStyle = () => <ExpandMore style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ExpandMore className="custom-class" />;
