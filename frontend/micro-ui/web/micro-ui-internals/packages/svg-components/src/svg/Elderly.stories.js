import React from "react";
import { Elderly } from "./Elderly";

export default {
  title: "Elderly",
  component: Elderly,
};

export const Default = () => <Elderly />;
export const Fill = () => <Elderly fill="blue" />;
export const Size = () => <Elderly height="50" width="50" />;
export const CustomStyle = () => <Elderly style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Elderly className="custom-class" />;
