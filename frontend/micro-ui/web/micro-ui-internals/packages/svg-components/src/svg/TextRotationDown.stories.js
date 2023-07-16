import React from "react";
import { TextRotationDown } from "./TextRotationDown";

export default {
  title: "TextRotationDown",
  component: TextRotationDown,
};

export const Default = () => <TextRotationDown />;
export const Fill = () => <TextRotationDown fill="blue" />;
export const Size = () => <TextRotationDown height="50" width="50" />;
export const CustomStyle = () => <TextRotationDown style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TextRotationDown className="custom-class" />;
