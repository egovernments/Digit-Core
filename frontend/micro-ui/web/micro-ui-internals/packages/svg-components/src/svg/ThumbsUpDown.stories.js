import React from "react";
import { ThumbsUpDown } from "./ThumbsUpDown";

export default {
  title: "ThumbsUpDown",
  component: ThumbsUpDown,
};

export const Default = () => <ThumbsUpDown />;
export const Fill = () => <ThumbsUpDown fill="blue" />;
export const Size = () => <ThumbsUpDown height="50" width="50" />;
export const CustomStyle = () => <ThumbsUpDown style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ThumbsUpDown className="custom-class" />;
