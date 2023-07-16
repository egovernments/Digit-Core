import React from "react";
import { ArrowUpward } from "./ArrowUpward";

export default {
  title: "ArrowUpward",
  component: ArrowUpward,
};

export const Default = () => <ArrowUpward />;
export const Fill = () => <ArrowUpward fill="blue" />;
export const Size = () => <ArrowUpward height="50" width="50" />;
export const CustomStyle = () => <ArrowUpward style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ArrowUpward className="custom-class" />;
