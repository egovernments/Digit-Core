import React from "react";
import { Groups } from "./Groups";

export default {
  title: "Groups",
  component: Groups,
};

export const Default = () => <Groups />;
export const Fill = () => <Groups fill="blue" />;
export const Size = () => <Groups height="50" width="50" />;
export const CustomStyle = () => <Groups style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Groups className="custom-class" />;
