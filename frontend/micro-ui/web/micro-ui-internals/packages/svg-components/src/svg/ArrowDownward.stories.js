import React from "react";
import { ArrowDownward } from "./ArrowDownward";

export default {
  title: "ArrowDownward",
  component: ArrowDownward,
};

export const Default = () => <ArrowDownward />;
export const Fill = () => <ArrowDownward fill="blue" />;
export const Size = () => <ArrowDownward height="50" width="50" />;
export const CustomStyle = () => <ArrowDownward style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ArrowDownward className="custom-class" />;
