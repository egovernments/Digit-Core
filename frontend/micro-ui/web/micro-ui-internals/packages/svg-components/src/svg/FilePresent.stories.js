import React from "react";
import { FilePresent } from "./FilePresent";

export default {
  title: "FilePresent",
  component: FilePresent,
};

export const Default = () => <FilePresent />;
export const Fill = () => <FilePresent fill="blue" />;
export const Size = () => <FilePresent height="50" width="50" />;
export const CustomStyle = () => <FilePresent style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <FilePresent className="custom-class" />;
