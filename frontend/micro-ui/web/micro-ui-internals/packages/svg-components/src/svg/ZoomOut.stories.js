import React from "react";
import { ZoomOut } from "./ZoomOut";

export default {
  title: "ZoomOut",
  component: ZoomOut,
};

export const Default = () => <ZoomOut />;
export const Fill = () => <ZoomOut fill="blue" />;
export const Size = () => <ZoomOut height="50" width="50" />;
export const CustomStyle = () => <ZoomOut style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ZoomOut className="custom-class" />;
