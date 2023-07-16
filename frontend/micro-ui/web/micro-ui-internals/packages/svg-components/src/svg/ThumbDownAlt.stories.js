import React from "react";
import { ThumbDownAlt } from "./ThumbDownAlt";

export default {
  title: "ThumbDownAlt",
  component: ThumbDownAlt,
};

export const Default = () => <ThumbDownAlt />;
export const Fill = () => <ThumbDownAlt fill="blue" />;
export const Size = () => <ThumbDownAlt height="50" width="50" />;
export const CustomStyle = () => <ThumbDownAlt style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ThumbDownAlt className="custom-class" />;
