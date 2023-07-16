import React from "react";
import { ThumbUpAlt } from "./ThumbUpAlt";

export default {
  title: "ThumbUpAlt",
  component: ThumbUpAlt,
};

export const Default = () => <ThumbUpAlt />;
export const Fill = () => <ThumbUpAlt fill="blue" />;
export const Size = () => <ThumbUpAlt height="50" width="50" />;
export const CustomStyle = () => <ThumbUpAlt style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ThumbUpAlt className="custom-class" />;
