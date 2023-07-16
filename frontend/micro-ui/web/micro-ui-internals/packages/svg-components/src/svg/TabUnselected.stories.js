import React from "react";
import { TabUnselected } from "./TabUnselected";

export default {
  title: "TabUnselected",
  component: TabUnselected,
};

export const Default = () => <TabUnselected />;
export const Fill = () => <TabUnselected fill="blue" />;
export const Size = () => <TabUnselected height="50" width="50" />;
export const CustomStyle = () => <TabUnselected style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TabUnselected className="custom-class" />;
