import React from "react";
import { Architecture } from "./Architecture";

export default {
  title: "Architecture",
  component: Architecture,
};

export const Default = () => <Architecture />;
export const Fill = () => <Architecture fill="blue" />;
export const Size = () => <Architecture height="50" width="50" />;
export const CustomStyle = () => <Architecture style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Architecture className="custom-class" />;
