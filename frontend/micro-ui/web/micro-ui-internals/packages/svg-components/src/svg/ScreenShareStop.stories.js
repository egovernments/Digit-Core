import React from "react";
import { ScreenShareStop } from "./ScreenShareStop";

export default {
  title: "ScreenShareStop",
  component: ScreenShareStop,
};

export const Default = () => <ScreenShareStop />;
export const Fill = () => <ScreenShareStop fill="blue" />;
export const Size = () => <ScreenShareStop height="50" width="50" />;
export const CustomStyle = () => <ScreenShareStop style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ScreenShareStop className="custom-class" />;
