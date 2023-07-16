import React from "react";
import { ArrowForwardIosAlt } from "./ArrowForwardIosAlt";

export default {
  title: "ArrowForwardIosAlt",
  component: ArrowForwardIosAlt,
};

export const Default = () => <ArrowForwardIosAlt />;
export const Fill = () => <ArrowForwardIosAlt fill="blue" />;
export const Size = () => <ArrowForwardIosAlt height="50" width="50" />;
export const CustomStyle = () => <ArrowForwardIosAlt style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ArrowForwardIosAlt className="custom-class" />;
