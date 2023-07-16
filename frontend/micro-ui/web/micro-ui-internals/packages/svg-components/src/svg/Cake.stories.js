import React from "react";
import { Cake } from "./Cake";

export default {
  title: "Cake",
  component: Cake,
};

export const Default = () => <Cake />;
export const Fill = () => <Cake fill="blue" />;
export const Size = () => <Cake height="50" width="50" />;
export const CustomStyle = () => <Cake style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Cake className="custom-class" />;
