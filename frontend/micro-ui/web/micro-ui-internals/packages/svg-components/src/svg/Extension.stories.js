import React from "react";
import { Extension } from "./Extension";

export default {
  title: "Extension",
  component: Extension,
};

export const Default = () => <Extension />;
export const Fill = () => <Extension fill="blue" />;
export const Size = () => <Extension height="50" width="50" />;
export const CustomStyle = () => <Extension style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Extension className="custom-class" />;
