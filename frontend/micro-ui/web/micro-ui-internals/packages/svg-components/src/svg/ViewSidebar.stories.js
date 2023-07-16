import React from "react";
import { ViewSidebar } from "./ViewSidebar";

export default {
  title: "ViewSidebar",
  component: ViewSidebar,
};

export const Default = () => <ViewSidebar />;
export const Fill = () => <ViewSidebar fill="blue" />;
export const Size = () => <ViewSidebar height="50" width="50" />;
export const CustomStyle = () => <ViewSidebar style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ViewSidebar className="custom-class" />;
