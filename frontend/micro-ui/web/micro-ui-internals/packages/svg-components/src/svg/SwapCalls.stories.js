import React from "react";
import { SwapCalls } from "./SwapCalls";

export default {
  title: "SwapCalls",
  component: SwapCalls,
};

export const Default = () => <SwapCalls />;
export const Fill = () => <SwapCalls fill="blue" />;
export const Size = () => <SwapCalls height="50" width="50" />;
export const CustomStyle = () => <SwapCalls style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SwapCalls className="custom-class" />;
