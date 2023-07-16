import React from "react";
import { ViewHeadline } from "./ViewHeadline";

export default {
  title: "ViewHeadline",
  component: ViewHeadline,
};

export const Default = () => <ViewHeadline />;
export const Fill = () => <ViewHeadline fill="blue" />;
export const Size = () => <ViewHeadline height="50" width="50" />;
export const CustomStyle = () => <ViewHeadline style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ViewHeadline className="custom-class" />;
