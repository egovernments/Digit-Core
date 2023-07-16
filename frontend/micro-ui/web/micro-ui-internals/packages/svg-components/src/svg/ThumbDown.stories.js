import React from "react";
import { ThumbDown } from "./ThumbDown";

export default {
  title: "ThumbDown",
  component: ThumbDown,
};

export const Default = () => <ThumbDown />;
export const Fill = () => <ThumbDown fill="blue" />;
export const Size = () => <ThumbDown height="50" width="50" />;
export const CustomStyle = () => <ThumbDown style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ThumbDown className="custom-class" />;
