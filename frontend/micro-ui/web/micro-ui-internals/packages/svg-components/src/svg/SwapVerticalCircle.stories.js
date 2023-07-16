import React from "react";
import { SwapVerticalCircle } from "./SwapVerticalCircle";

export default {
  title: "SwapVerticalCircle",
  component: SwapVerticalCircle,
};

export const Default = () => <SwapVerticalCircle />;
export const Fill = () => <SwapVerticalCircle fill="blue" />;
export const Size = () => <SwapVerticalCircle height="50" width="50" />;
export const CustomStyle = () => <SwapVerticalCircle style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SwapVerticalCircle className="custom-class" />;
