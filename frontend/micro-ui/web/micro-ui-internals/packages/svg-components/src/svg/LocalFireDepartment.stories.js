import React from "react";
import { LocalFireDepartment } from "./LocalFireDepartment";

export default {
  title: "LocalFireDepartment",
  component: LocalFireDepartment,
};

export const Default = () => <LocalFireDepartment />;
export const Fill = () => <LocalFireDepartment fill="blue" />;
export const Size = () => <LocalFireDepartment height="50" width="50" />;
export const CustomStyle = () => <LocalFireDepartment style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LocalFireDepartment className="custom-class" />;
