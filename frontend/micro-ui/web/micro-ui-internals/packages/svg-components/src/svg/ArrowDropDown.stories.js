import React from "react";
import { ArrowDropDown } from "./ArrowDropDown";

export default {
  title: "ArrowDropDown",
  component: ArrowDropDown,
};

export const Default = () => <ArrowDropDown />;
export const Fill = () => <ArrowDropDown fill="blue" />;
export const Size = () => <ArrowDropDown height="50" width="50" />;
export const CustomStyle = () => <ArrowDropDown style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ArrowDropDown className="custom-class" />;
