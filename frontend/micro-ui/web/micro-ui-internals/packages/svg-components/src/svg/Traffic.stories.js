import React from "react";
import { Traffic } from "./Traffic";

export default {
  title: "Traffic",
  component: Traffic,
};

export const Default = () => <Traffic />;
export const Fill = () => <Traffic fill="blue" />;
export const Size = () => <Traffic height="50" width="50" />;
export const CustomStyle = () => <Traffic style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Traffic className="custom-class" />;
