import React from "react";
import { UnfoldMore } from "./UnfoldMore";

export default {
  title: "UnfoldMore",
  component: UnfoldMore,
};

export const Default = () => <UnfoldMore />;
export const Fill = () => <UnfoldMore fill="blue" />;
export const Size = () => <UnfoldMore height="50" width="50" />;
export const CustomStyle = () => <UnfoldMore style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <UnfoldMore className="custom-class" />;
