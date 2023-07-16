import React from "react";
import { Train } from "./Train";

export default {
  title: "Train",
  component: Train,
};

export const Default = () => <Train />;
export const Fill = () => <Train fill="blue" />;
export const Size = () => <Train height="50" width="50" />;
export const CustomStyle = () => <Train style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Train className="custom-class" />;
