import React from "react";
import { SwapHorizontal } from "./SwapHorizontal";

export default {
  title: "SwapHorizontal",
  component: SwapHorizontal,
};

export const Default = () => <SwapHorizontal />;
export const Fill = () => <SwapHorizontal fill="blue" />;
export const Size = () => <SwapHorizontal height="50" width="50" />;
export const CustomStyle = () => <SwapHorizontal style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SwapHorizontal className="custom-class" />;
