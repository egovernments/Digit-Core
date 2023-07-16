import React from "react";
import { TextRotationAngleDown } from "./TextRotationAngleDown";

export default {
  title: "TextRotationAngleDown",
  component: TextRotationAngleDown,
};

export const Default = () => <TextRotationAngleDown />;
export const Fill = () => <TextRotationAngleDown fill="blue" />;
export const Size = () => <TextRotationAngleDown height="50" width="50" />;
export const CustomStyle = () => <TextRotationAngleDown style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TextRotationAngleDown className="custom-class" />;
