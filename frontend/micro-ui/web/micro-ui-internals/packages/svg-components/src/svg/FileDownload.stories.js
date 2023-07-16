import React from "react";
import { FileDownload } from "./FileDownload";

export default {
  title: "FileDownload",
  component: FileDownload,
};

export const Default = () => <FileDownload />;
export const Fill = () => <FileDownload fill="blue" />;
export const Size = () => <FileDownload height="50" width="50" />;
export const CustomStyle = () => <FileDownload style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <FileDownload className="custom-class" />;
