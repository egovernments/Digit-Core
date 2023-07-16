import React from "react";
import { LocalCarWash } from "./LocalCarWash";

export default {
  title: "LocalCarWash",
  component: LocalCarWash,
};

export const Default = () => <LocalCarWash />;
export const Fill = () => <LocalCarWash fill="blue" />;
export const Size = () => <LocalCarWash height="50" width="50" />;
export const CustomStyle = () => <LocalCarWash style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LocalCarWash className="custom-class" />;
