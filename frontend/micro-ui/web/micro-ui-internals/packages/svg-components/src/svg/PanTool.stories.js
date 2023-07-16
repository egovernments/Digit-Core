import React from "react";
import { PanTool } from "./PanTool";

export default {
  title: "PanTool",
  component: PanTool,
};

export const Default = () => <PanTool />;
export const Fill = () => <PanTool fill="blue" />;
export const Size = () => <PanTool height="50" width="50" />;
export const CustomStyle = () => <PanTool style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PanTool className="custom-class" />;
