import React from "react";
import { Tab } from "./Tab";

export default {
  title: "Tab",
  component: Tab,
};

export const Default = () => <Tab />;
export const Fill = () => <Tab fill="blue" />;
export const Size = () => <Tab height="50" width="50" />;
export const CustomStyle = () => <Tab style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Tab className="custom-class" />;
