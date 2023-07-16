import React from "react";
import { Torch } from "./Torch";

export default {
  title: "Torch",
  component: Torch,
};

export const Default = () => <Torch />;
export const Fill = () => <Torch fill="blue" />;
export const Size = () => <Torch height="50" width="50" />;
export const CustomStyle = () => <Torch style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Torch className="custom-class" />;
