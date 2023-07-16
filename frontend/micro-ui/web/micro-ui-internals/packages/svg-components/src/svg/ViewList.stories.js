import React from "react";
import { ViewList } from "./ViewList";

export default {
  title: "ViewList",
  component: ViewList,
};

export const Default = () => <ViewList />;
export const Fill = () => <ViewList fill="blue" />;
export const Size = () => <ViewList height="50" width="50" />;
export const CustomStyle = () => <ViewList style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ViewList className="custom-class" />;
