import React from "react";
import { ArrowRight } from "./ArrowRight";

export default {
  title: "ArrowRight",
  component: ArrowRight,
};

export const Default = () => <ArrowRight />;
export const Fill = () => <ArrowRight fill="blue" />;
export const Size = () => <ArrowRight height="50" width="50" />;
export const CustomStyle = () => <ArrowRight style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ArrowRight className="custom-class" />;
