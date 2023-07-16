import React from "react";
import { Update } from "./Update";

export default {
  title: "Update",
  component: Update,
};

export const Default = () => <Update />;
export const Fill = () => <Update fill="blue" />;
export const Size = () => <Update height="50" width="50" />;
export const CustomStyle = () => <Update style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Update className="custom-class" />;
