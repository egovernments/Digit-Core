import React from "react";
import { GridView } from "./GridView";

export default {
  title: "GridView",
  component: GridView,
};

export const Default = () => <GridView />;
export const Fill = () => <GridView fill="blue" />;
export const Size = () => <GridView height="50" width="50" />;
export const CustomStyle = () => <GridView style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <GridView className="custom-class" />;
