import React from "react";
import { Attractions } from "./Attractions";

export default {
  title: "Attractions",
  component: Attractions,
};

export const Default = () => <Attractions />;
export const Fill = () => <Attractions fill="blue" />;
export const Size = () => <Attractions height="50" width="50" />;
export const CustomStyle = () => <Attractions style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Attractions className="custom-class" />;
