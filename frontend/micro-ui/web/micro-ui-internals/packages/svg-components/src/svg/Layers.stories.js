import React from "react";
import { Layers } from "./Layers";

export default {
  title: "Layers",
  component: Layers,
};

export const Default = () => <Layers />;
export const Fill = () => <Layers fill="blue" />;
export const Size = () => <Layers height="50" width="50" />;
export const CustomStyle = () => <Layers style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Layers className="custom-class" />;
