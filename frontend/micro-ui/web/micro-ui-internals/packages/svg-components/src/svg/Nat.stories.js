import React from "react";
import { Nat } from "./Nat";

export default {
  title: "Nat",
  component: Nat,
};

export const Default = () => <Nat />;
export const Fill = () => <Nat fill="blue" />;
export const Size = () => <Nat height="50" width="50" />;
export const CustomStyle = () => <Nat style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Nat className="custom-class" />;
