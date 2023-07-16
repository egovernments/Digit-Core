import React from "react";
import { Wysiwyg } from "./Wysiwyg";

export default {
  title: "Wysiwyg",
  component: Wysiwyg,
};

export const Default = () => <Wysiwyg />;
export const Fill = () => <Wysiwyg fill="blue" />;
export const Size = () => <Wysiwyg height="50" width="50" />;
export const CustomStyle = () => <Wysiwyg style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Wysiwyg className="custom-class" />;
