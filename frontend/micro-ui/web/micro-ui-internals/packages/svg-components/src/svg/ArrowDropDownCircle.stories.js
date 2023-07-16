import React from "react";
import { ArrowDropDownCircle } from "./ArrowDropDownCircle";

export default {
  title: "ArrowDropDownCircle",
  component: ArrowDropDownCircle,
};

export const Default = () => <ArrowDropDownCircle />;
export const Fill = () => <ArrowDropDownCircle fill="blue" />;
export const Size = () => <ArrowDropDownCircle height="50" width="50" />;
export const CustomStyle = () => <ArrowDropDownCircle style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ArrowDropDownCircle className="custom-class" />;
