import React from "react";
import { TrendingFlat } from "./TrendingFlat";

export default {
  title: "TrendingFlat",
  component: TrendingFlat,
};

export const Default = () => <TrendingFlat />;
export const Fill = () => <TrendingFlat fill="blue" />;
export const Size = () => <TrendingFlat height="50" width="50" />;
export const CustomStyle = () => <TrendingFlat style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TrendingFlat className="custom-class" />;
