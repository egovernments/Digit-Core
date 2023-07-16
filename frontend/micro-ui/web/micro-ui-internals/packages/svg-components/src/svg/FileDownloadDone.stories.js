import React from "react";
import { FileDownloadDone } from "./FileDownloadDone";

export default {
  title: "FileDownloadDone",
  component: FileDownloadDone,
};

export const Default = () => <FileDownloadDone />;
export const Fill = () => <FileDownloadDone fill="blue" />;
export const Size = () => <FileDownloadDone height="50" width="50" />;
export const CustomStyle = () => <FileDownloadDone style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <FileDownloadDone className="custom-class" />;
