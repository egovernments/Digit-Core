import React from "react";
import { LabelImportantOutline } from "./LabelImportantOutline";

export default {
  title: "LabelImportantOutline",
  component: LabelImportantOutline,
};

export const Default = () => <LabelImportantOutline />;
export const Fill = () => <LabelImportantOutline fill="blue" />;
export const Size = () => <LabelImportantOutline height="50" width="50" />;
export const CustomStyle = () => <LabelImportantOutline style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LabelImportantOutline className="custom-class" />;
