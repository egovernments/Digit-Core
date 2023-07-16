import React from "react";
import { Festival } from "./Festival";

export default {
  title: "Festival",
  component: Festival,
};

export const Default = () => <Festival />;
export const Fill = () => <Festival fill="blue" />;
export const Size = () => <Festival height="50" width="50" />;
export const CustomStyle = () => <Festival style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Festival className="custom-class" />;
