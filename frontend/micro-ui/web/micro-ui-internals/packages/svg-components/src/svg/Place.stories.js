import React from "react";
import { Place } from "./Place";

export default {
  title: "Place",
  component: Place,
};

export const Default = () => <Place />;
export const Fill = () => <Place fill="blue" />;
export const Size = () => <Place height="50" width="50" />;
export const CustomStyle = () => <Place style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Place className="custom-class" />;
