import React from "react";
import { CloseFullscreen } from "./CloseFullscreen";

export default {
  title: "CloseFullscreen",
  component: CloseFullscreen,
};

export const Default = () => <CloseFullscreen />;
export const Fill = () => <CloseFullscreen fill="blue" />;
export const Size = () => <CloseFullscreen height="50" width="50" />;
export const CustomStyle = () => <CloseFullscreen style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <CloseFullscreen className="custom-class" />;
