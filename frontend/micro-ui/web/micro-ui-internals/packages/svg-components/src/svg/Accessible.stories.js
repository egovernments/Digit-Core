import React from "react";
import { Accessible } from "./Accessible";

export default {
  title: "Accessible",
  component: Accessible,
};

export const Default = () => <Accessible />;
export const Fill = () => <Accessible fill="blue" />;
export const Size = () => <Accessible height="50" width="50" />;
export const CustomStyle = () => <Accessible style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Accessible className="custom-class" />;
