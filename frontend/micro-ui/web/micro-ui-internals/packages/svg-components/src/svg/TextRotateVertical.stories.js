import React from "react";
import { TextRotateVertical } from "./TextRotateVertical";

export default {
  title: "TextRotateVertical",
  component: TextRotateVertical,
};

export const Default = () => <TextRotateVertical />;
export const Fill = () => <TextRotateVertical fill="blue" />;
export const Size = () => <TextRotateVertical height="50" width="50" />;
export const CustomStyle = () => <TextRotateVertical style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TextRotateVertical className="custom-class" />;
