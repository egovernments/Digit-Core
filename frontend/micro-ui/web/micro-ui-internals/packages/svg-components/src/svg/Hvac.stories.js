import React from "react";
import { Hvac } from "./Hvac";

export default {
  title: "Hvac",
  component: Hvac,
};

export const Default = () => <Hvac />;
export const Fill = () => <Hvac fill="blue" />;
export const Size = () => <Hvac height="50" width="50" />;
export const CustomStyle = () => <Hvac style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Hvac className="custom-class" />;
