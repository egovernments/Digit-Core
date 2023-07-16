import React from "react";
import { TextRotationNone } from "./TextRotationNone";

export default {
  title: "TextRotationNone",
  component: TextRotationNone,
};

export const Default = () => <TextRotationNone />;
export const Fill = () => <TextRotationNone fill="blue" />;
export const Size = () => <TextRotationNone height="50" width="50" />;
export const CustomStyle = () => <TextRotationNone style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TextRotationNone className="custom-class" />;
