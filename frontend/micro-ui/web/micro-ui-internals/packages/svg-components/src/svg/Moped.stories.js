import React from "react";
import { Moped } from "./Moped";

export default {
  title: "Moped",
  component: Moped,
};

export const Default = () => <Moped />;
export const Fill = () => <Moped fill="blue" />;
export const Size = () => <Moped height="50" width="50" />;
export const CustomStyle = () => <Moped style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Moped className="custom-class" />;
