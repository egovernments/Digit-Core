import React from "react";
import { ZoomIn } from "./ZoomIn";

export default {
  title: "ZoomIn",
  component: ZoomIn,
};

export const Default = () => <ZoomIn />;
export const Fill = () => <ZoomIn fill="blue" />;
export const Size = () => <ZoomIn height="50" width="50" />;
export const CustomStyle = () => <ZoomIn style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ZoomIn className="custom-class" />;
