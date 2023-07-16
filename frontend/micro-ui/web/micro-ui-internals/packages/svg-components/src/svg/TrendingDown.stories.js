import React from "react";
import { TrendingDown } from "./TrendingDown";

export default {
  title: "TrendingDown",
  component: TrendingDown,
};

export const Default = () => <TrendingDown />;
export const Fill = () => <TrendingDown fill="blue" />;
export const Size = () => <TrendingDown height="50" width="50" />;
export const CustomStyle = () => <TrendingDown style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TrendingDown className="custom-class" />;
