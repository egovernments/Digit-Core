import React from "react";
import { ViewWeek } from "./ViewWeek";

export default {
  title: "ViewWeek",
  component: ViewWeek,
};

export const Default = () => <ViewWeek />;
export const Fill = () => <ViewWeek fill="blue" />;
export const Size = () => <ViewWeek height="50" width="50" />;
export const CustomStyle = () => <ViewWeek style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ViewWeek className="custom-class" />;
