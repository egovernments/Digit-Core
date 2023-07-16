import React from "react";
import { Population } from "./Population";

export default {
  title: "Population",
  component: Population,
};

export const Default = () => <Population />;
export const Fill = () => <Population fill="blue" />;
export const Size = () => <Population height="50" width="50" />;
export const CustomStyle = () => <Population style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Population className="custom-class" />;
