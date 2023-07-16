import React from "react";
import { Work } from "./Work";

export default {
  title: "Work",
  component: Work,
};

export const Default = () => <Work />;
export const Fill = () => <Work fill="blue" />;
export const Size = () => <Work height="50" width="50" />;
export const CustomStyle = () => <Work style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Work className="custom-class" />;
