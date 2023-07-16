import React from "react";
import { ViewStream } from "./ViewStream";

export default {
  title: "ViewStream",
  component: ViewStream,
};

export const Default = () => <ViewStream />;
export const Fill = () => <ViewStream fill="blue" />;
export const Size = () => <ViewStream height="50" width="50" />;
export const CustomStyle = () => <ViewStream style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ViewStream className="custom-class" />;
