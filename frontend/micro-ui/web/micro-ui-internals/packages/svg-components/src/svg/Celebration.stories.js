import React from "react";
import { Celebration } from "./Celebration";

export default {
  title: "Celebration",
  component: Celebration,
};

export const Default = () => <Celebration />;
export const Fill = () => <Celebration fill="blue" />;
export const Size = () => <Celebration height="50" width="50" />;
export const CustomStyle = () => <Celebration style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Celebration className="custom-class" />;
