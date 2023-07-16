import React from "react";
import { CloudUpload } from "./CloudUpload";

export default {
  title: "CloudUpload",
  component: CloudUpload,
};

export const Default = () => <CloudUpload />;
export const Fill = () => <CloudUpload fill="blue" />;
export const Size = () => <CloudUpload height="50" width="50" />;
export const CustomStyle = () => <CloudUpload style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <CloudUpload className="custom-class" />;
