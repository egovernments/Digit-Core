import React from "react";
import { Fireplace } from "./Fireplace";

export default {
  title: "Fireplace",
  component: Fireplace,
};

export const Default = () => <Fireplace />;
export const Fill = () => <Fireplace fill="blue" />;
export const Size = () => <Fireplace height="50" width="50" />;
export const CustomStyle = () => <Fireplace style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Fireplace className="custom-class" />;
