import React from "react";
import { LocalDining } from "./LocalDining";

export default {
  title: "LocalDining",
  component: LocalDining,
};

export const Default = () => <LocalDining />;
export const Fill = () => <LocalDining fill="blue" />;
export const Size = () => <LocalDining height="50" width="50" />;
export const CustomStyle = () => <LocalDining style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LocalDining className="custom-class" />;
