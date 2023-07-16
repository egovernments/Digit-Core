import React from "react";
import { UnfoldLess } from "./UnfoldLess";

export default {
  title: "UnfoldLess",
  component: UnfoldLess,
};

export const Default = () => <UnfoldLess />;
export const Fill = () => <UnfoldLess fill="blue" />;
export const Size = () => <UnfoldLess height="50" width="50" />;
export const CustomStyle = () => <UnfoldLess style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <UnfoldLess className="custom-class" />;
