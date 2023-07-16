import React from "react";
import { BeenHere } from "./BeenHere";

export default {
  title: "BeenHere",
  component: BeenHere,
};

export const Default = () => <BeenHere />;
export const Fill = () => <BeenHere fill="blue" />;
export const Size = () => <BeenHere height="50" width="50" />;
export const CustomStyle = () => <BeenHere style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <BeenHere className="custom-class" />;
