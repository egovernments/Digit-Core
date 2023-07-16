import React from "react";
import { UploadFile } from "./UploadFile";

export default {
  title: "UploadFile",
  component: UploadFile,
};

export const Default = () => <UploadFile />;
export const Fill = () => <UploadFile fill="blue" />;
export const Size = () => <UploadFile height="50" width="50" />;
export const CustomStyle = () => <UploadFile style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <UploadFile className="custom-class" />;
