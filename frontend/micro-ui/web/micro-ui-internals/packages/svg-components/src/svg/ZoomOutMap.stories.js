import React from "react";
import { ZoomOutMap } from "./ZoomOutMap";

export default {
  title: "ZoomOutMap",
  component: ZoomOutMap,
};

export const Default = () => <ZoomOutMap />;
export const Fill = () => <ZoomOutMap fill="blue" />;
export const Size = () => <ZoomOutMap height="50" width="50" />;
export const CustomStyle = () => <ZoomOutMap style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ZoomOutMap className="custom-class" />;
