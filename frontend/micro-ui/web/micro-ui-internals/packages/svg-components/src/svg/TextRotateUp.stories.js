import React from "react";
import { TextRotateUp } from "./TextRotateUp";

export default {
  title: "TextRotateUp",
  component: TextRotateUp,
};

export const Default = () => <TextRotateUp />;
export const Fill = () => <TextRotateUp fill="blue" />;
export const Size = () => <TextRotateUp height="50" width="50" />;
export const CustomStyle = () => <TextRotateUp style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TextRotateUp className="custom-class" />;
