import React from "react";
import { ViewColumn } from "./ViewColumn";

export default {
  title: "ViewColumn",
  component: ViewColumn,
};

export const Default = () => <ViewColumn />;
export const Fill = () => <ViewColumn fill="blue" />;
export const Size = () => <ViewColumn height="50" width="50" />;
export const CustomStyle = () => <ViewColumn style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ViewColumn className="custom-class" />;
