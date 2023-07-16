import React from "react";
import { PublicOff } from "./PublicOff";

export default {
  title: "PublicOff",
  component: PublicOff,
};

export const Default = () => <PublicOff />;
export const Fill = () => <PublicOff fill="blue" />;
export const Size = () => <PublicOff height="50" width="50" />;
export const CustomStyle = () => <PublicOff style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PublicOff className="custom-class" />;
