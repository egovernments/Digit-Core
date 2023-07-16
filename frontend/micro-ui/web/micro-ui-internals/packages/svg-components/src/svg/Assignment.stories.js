import React from "react";
import { Assignment } from "./Assignment";

export default {
  title: "Assignment",
  component: Assignment,
};

export const Default = () => <Assignment />;
export const Fill = () => <Assignment fill="blue" />;
export const Size = () => <Assignment height="50" width="50" />;
export const CustomStyle = () => <Assignment style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Assignment className="custom-class" />;
