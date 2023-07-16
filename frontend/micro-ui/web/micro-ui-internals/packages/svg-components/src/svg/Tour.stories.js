import React from "react";
import { Tour } from "./Tour";

export default {
  title: "Tour",
  component: Tour,
};

export const Default = () => <Tour />;
export const Fill = () => <Tour fill="blue" />;
export const Size = () => <Tour height="50" width="50" />;
export const CustomStyle = () => <Tour style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Tour className="custom-class" />;
