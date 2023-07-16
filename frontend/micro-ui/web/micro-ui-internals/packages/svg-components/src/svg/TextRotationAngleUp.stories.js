import React from "react";
import { TextRotationAngleUp } from "./TextRotationAngleUp";

export default {
  title: "TextRotationAngleUp",
  component: TextRotationAngleUp,
};

export const Default = () => <TextRotationAngleUp />;
export const Fill = () => <TextRotationAngleUp fill="blue" />;
export const Size = () => <TextRotationAngleUp height="50" width="50" />;
export const CustomStyle = () => <TextRotationAngleUp style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TextRotationAngleUp className="custom-class" />;
