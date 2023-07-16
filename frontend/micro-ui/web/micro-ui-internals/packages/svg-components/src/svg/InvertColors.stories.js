import React from "react";
import { InvertColors } from "./InvertColors";

export default {
  title: "InvertColors",
  component: InvertColors,
};

export const Default = () => <InvertColors />;
export const Fill = () => <InvertColors fill="blue" />;
export const Size = () => <InvertColors height="50" width="50" />;
export const CustomStyle = () => <InvertColors style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <InvertColors className="custom-class" />;
