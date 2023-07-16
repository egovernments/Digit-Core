import React from "react";
import { ArrowCircleUp } from "./ArrowCircleUp";

export default {
  title: "ArrowCircleUp",
  component: ArrowCircleUp,
};

export const Default = () => <ArrowCircleUp />;
export const Fill = () => <ArrowCircleUp fill="blue" />;
export const Size = () => <ArrowCircleUp height="50" width="50" />;
export const CustomStyle = () => <ArrowCircleUp style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ArrowCircleUp className="custom-class" />;
