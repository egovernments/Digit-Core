import React from "react";
import { Commute } from "./Commute";

export default {
  title: "Commute",
  component: Commute,
};

export const Default = () => <Commute />;
export const Fill = () => <Commute fill="blue" />;
export const Size = () => <Commute height="50" width="50" />;
export const CustomStyle = () => <Commute style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Commute className="custom-class" />;
