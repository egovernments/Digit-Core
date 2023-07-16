import React from "react";
import { SwapHorizontalCircle } from "./SwapHorizontalCircle";

export default {
  title: "SwapHorizontalCircle",
  component: SwapHorizontalCircle,
};

export const Default = () => <SwapHorizontalCircle />;
export const Fill = () => <SwapHorizontalCircle fill="blue" />;
export const Size = () => <SwapHorizontalCircle height="50" width="50" />;
export const CustomStyle = () => <SwapHorizontalCircle style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SwapHorizontalCircle className="custom-class" />;
