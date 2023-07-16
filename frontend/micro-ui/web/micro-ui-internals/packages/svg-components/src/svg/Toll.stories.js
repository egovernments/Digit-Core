import React from "react";
import { Toll } from "./Toll";

export default {
  title: "Toll",
  component: Toll,
};

export const Default = () => <Toll />;
export const Fill = () => <Toll fill="blue" />;
export const Size = () => <Toll height="50" width="50" />;
export const CustomStyle = () => <Toll style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Toll className="custom-class" />;
