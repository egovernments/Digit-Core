import React from "react";
import { TextSnippet } from "./TextSnippet";

export default {
  title: "TextSnippet",
  component: TextSnippet,
};

export const Default = () => <TextSnippet />;
export const Fill = () => <TextSnippet fill="blue" />;
export const Size = () => <TextSnippet height="50" width="50" />;
export const CustomStyle = () => <TextSnippet style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TextSnippet className="custom-class" />;
