import React from "react";
import { Bookmark } from "./Bookmark";

export default {
  title: "Bookmark",
  component: Bookmark,
};

export const Default = () => <Bookmark />;
export const Fill = () => <Bookmark fill="blue" />;
export const Size = () => <Bookmark height="50" width="50" />;
export const CustomStyle = () => <Bookmark style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Bookmark className="custom-class" />;
