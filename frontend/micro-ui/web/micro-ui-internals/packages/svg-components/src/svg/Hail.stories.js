import React from "react";
import { Hail } from "./Hail";

export default {
  title: "Hail",
  component: Hail,
};

export const Default = () => <Hail />;
export const Fill = () => <Hail fill="blue" />;
export const Size = () => <Hail height="50" width="50" />;
export const CustomStyle = () => <Hail style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Hail className="custom-class" />;
