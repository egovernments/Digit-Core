import React from "react";
import { ThumbDownOffAlt } from "./ThumbDownOffAlt";

export default {
  title: "ThumbDownOffAlt",
  component: ThumbDownOffAlt,
};

export const Default = () => <ThumbDownOffAlt />;
export const Fill = () => <ThumbDownOffAlt fill="blue" />;
export const Size = () => <ThumbDownOffAlt height="50" width="50" />;
export const CustomStyle = () => <ThumbDownOffAlt style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ThumbDownOffAlt className="custom-class" />;
