import React from "react";
import { Sick } from "./Sick";

export default {
  title: "Sick",
  component: Sick,
};

export const Default = () => <Sick />;
export const Fill = () => <Sick fill="blue" />;
export const Size = () => <Sick height="50" width="50" />;
export const CustomStyle = () => <Sick style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Sick className="custom-class" />;
