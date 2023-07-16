import React from "react";
import { SnippetFolder } from "./SnippetFolder";

export default {
  title: "SnippetFolder",
  component: SnippetFolder,
};

export const Default = () => <SnippetFolder />;
export const Fill = () => <SnippetFolder fill="blue" />;
export const Size = () => <SnippetFolder height="50" width="50" />;
export const CustomStyle = () => <SnippetFolder style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SnippetFolder className="custom-class" />;
