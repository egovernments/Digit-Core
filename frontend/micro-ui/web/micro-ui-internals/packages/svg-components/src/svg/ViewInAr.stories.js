import React from "react";
import { ViewInAr } from "./ViewInAr";

export default {
  title: "ViewInAr",
  component: ViewInAr,
};

export const Default = () => <ViewInAr />;
export const Fill = () => <ViewInAr fill="blue" />;
export const Size = () => <ViewInAr height="50" width="50" />;
export const CustomStyle = () => <ViewInAr style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ViewInAr className="custom-class" />;
