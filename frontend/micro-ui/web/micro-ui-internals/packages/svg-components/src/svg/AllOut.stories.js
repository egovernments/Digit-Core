import React from "react";
import { AllOut } from "./AllOut";

export default {
  title: "AllOut",
  component: AllOut,
};

export const Default = () => <AllOut />;
export const Fill = () => <AllOut fill="blue" />;
export const Size = () => <AllOut height="50" width="50" />;
export const CustomStyle = () => <AllOut style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AllOut className="custom-class" />;
