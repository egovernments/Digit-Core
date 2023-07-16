import React from "react";
import { TrendingUp } from "./TrendingUp";

export default {
  title: "TrendingUp",
  component: TrendingUp,
};

export const Default = () => <TrendingUp />;
export const Fill = () => <TrendingUp fill="blue" />;
export const Size = () => <TrendingUp height="50" width="50" />;
export const CustomStyle = () => <TrendingUp style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TrendingUp className="custom-class" />;
