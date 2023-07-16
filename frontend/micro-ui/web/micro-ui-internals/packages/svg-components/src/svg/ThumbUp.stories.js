import React from "react";
import { ThumbUp } from "./ThumbUp";

export default {
  title: "ThumbUp",
  component: ThumbUp,
};

export const Default = () => <ThumbUp />;
export const Fill = () => <ThumbUp fill="blue" />;
export const Size = () => <ThumbUp height="50" width="50" />;
export const CustomStyle = () => <ThumbUp style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ThumbUp className="custom-class" />;
