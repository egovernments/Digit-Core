import React from "react";
import { ChevronRight } from "./ChevronRight";

export default {
  title: "ChevronRight",
  component: ChevronRight,
};

export const Default = () => <ChevronRight />;
export const Fill = () => <ChevronRight fill="blue" />;
export const Size = () => <ChevronRight height="50" width="50" />;
export const CustomStyle = () => <ChevronRight style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ChevronRight className="custom-class" />;
