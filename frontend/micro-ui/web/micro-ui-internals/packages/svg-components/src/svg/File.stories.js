import React from "react";
import { File } from "./File";

export default {
  title: "File",
  component: File,
};

export const Default = () => <File />;
export const Fill = () => <File fill="blue" />;
export const Size = () => <File height="50" width="50" />;
export const CustomStyle = () => <File style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <File className="custom-class" />;
