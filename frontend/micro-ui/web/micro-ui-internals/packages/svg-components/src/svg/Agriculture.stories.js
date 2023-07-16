import React from "react";
import { Agriculture } from "./Agriculture";

export default {
  title: "Agriculture",
  component: Agriculture,
};

export const Default = () => <Agriculture />;
export const Fill = () => <Agriculture fill="blue" />;
export const Size = () => <Agriculture height="50" width="50" />;
export const CustomStyle = () => <Agriculture style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Agriculture className="custom-class" />;
