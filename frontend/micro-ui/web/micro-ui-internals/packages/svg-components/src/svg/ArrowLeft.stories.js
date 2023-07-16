import React from "react";
import { ArrowLeft } from "./ArrowLeft";

export default {
  title: "ArrowLeft",
  component: ArrowLeft,
};

export const Default = () => <ArrowLeft />;
export const Fill = () => <ArrowLeft fill="blue" />;
export const Size = () => <ArrowLeft height="50" width="50" />;
export const CustomStyle = () => <ArrowLeft style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ArrowLeft className="custom-class" />;
