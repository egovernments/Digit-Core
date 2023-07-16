import React from "react";
import { ThumbUpOffAlt } from "./ThumbUpOffAlt";

export default {
  title: "ThumbUpOffAlt",
  component: ThumbUpOffAlt,
};

export const Default = () => <ThumbUpOffAlt />;
export const Fill = () => <ThumbUpOffAlt fill="blue" />;
export const Size = () => <ThumbUpOffAlt height="50" width="50" />;
export const CustomStyle = () => <ThumbUpOffAlt style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ThumbUpOffAlt className="custom-class" />;
