import React from "react";
import { Toc } from "./Toc";

export default {
  title: "Toc",
  component: Toc,
};

export const Default = () => <Toc />;
export const Fill = () => <Toc fill="blue" />;
export const Size = () => <Toc height="50" width="50" />;
export const CustomStyle = () => <Toc style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Toc className="custom-class" />;
