import React from "react";
import { ArrowDropUp } from "./ArrowDropUp";

export default {
  title: "ArrowDropUp",
  component: ArrowDropUp,
};

export const Default = () => <ArrowDropUp />;
export const Fill = () => <ArrowDropUp fill="blue" />;
export const Size = () => <ArrowDropUp height="50" width="50" />;
export const CustomStyle = () => <ArrowDropUp style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ArrowDropUp className="custom-class" />;
