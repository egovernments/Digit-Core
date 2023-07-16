import React from "react";
import { AutoDelete } from "./AutoDelete";

export default {
  title: "AutoDelete",
  component: AutoDelete,
};

export const Default = () => <AutoDelete />;
export const Fill = () => <AutoDelete fill="blue" />;
export const Size = () => <AutoDelete height="50" width="50" />;
export const CustomStyle = () => <AutoDelete style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AutoDelete className="custom-class" />;
