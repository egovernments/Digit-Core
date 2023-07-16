import React from "react";
import { ArrowForward } from "./ArrowForward";

export default {
  title: "ArrowForward",
  component: ArrowForward,
};

export const Default = () => <ArrowForward />;
export const Fill = () => <ArrowForward fill="blue" />;
export const Size = () => <ArrowForward height="50" width="50" />;
export const CustomStyle = () => <ArrowForward style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ArrowForward className="custom-class" />;
