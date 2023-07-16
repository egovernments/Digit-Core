import React from "react";
import { Science } from "./Science";

export default {
  title: "Science",
  component: Science,
};

export const Default = () => <Science />;
export const Fill = () => <Science fill="blue" />;
export const Size = () => <Science height="50" width="50" />;
export const CustomStyle = () => <Science style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Science className="custom-class" />;
