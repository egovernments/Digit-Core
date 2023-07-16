import React from "react";
import { ViewDay } from "./ViewDay";

export default {
  title: "ViewDay",
  component: ViewDay,
};

export const Default = () => <ViewDay />;
export const Fill = () => <ViewDay fill="blue" />;
export const Size = () => <ViewDay height="50" width="50" />;
export const CustomStyle = () => <ViewDay style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ViewDay className="custom-class" />;
