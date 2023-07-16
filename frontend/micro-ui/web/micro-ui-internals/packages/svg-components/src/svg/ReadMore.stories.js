import React from "react";
import { ReadMore } from "./ReadMore";

export default {
  title: "ReadMore",
  component: ReadMore,
};

export const Default = () => <ReadMore />;
export const Fill = () => <ReadMore fill="blue" />;
export const Size = () => <ReadMore height="50" width="50" />;
export const CustomStyle = () => <ReadMore style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ReadMore className="custom-class" />;
