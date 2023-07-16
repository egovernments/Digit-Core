import React from "react";
import { AspectRatio } from "./AspectRatio";

export default {
  title: "AspectRatio",
  component: AspectRatio,
};

export const Default = () => <AspectRatio />;
export const Fill = () => <AspectRatio fill="blue" />;
export const Size = () => <AspectRatio height="50" width="50" />;
export const CustomStyle = () => <AspectRatio style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AspectRatio className="custom-class" />;
