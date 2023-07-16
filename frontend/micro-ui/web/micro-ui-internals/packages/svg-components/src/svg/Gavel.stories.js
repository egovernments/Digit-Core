import React from "react";
import { Gavel } from "./Gavel";

export default {
  title: "Gavel",
  component: Gavel,
};

export const Default = () => <Gavel />;
export const Fill = () => <Gavel fill="blue" />;
export const Size = () => <Gavel height="50" width="50" />;
export const CustomStyle = () => <Gavel style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Gavel className="custom-class" />;
