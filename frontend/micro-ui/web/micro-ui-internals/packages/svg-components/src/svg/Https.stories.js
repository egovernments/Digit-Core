import React from "react";
import { Https } from "./Https";

export default {
  title: "Https",
  component: Https,
};

export const Default = () => <Https />;
export const Fill = () => <Https fill="blue" />;
export const Size = () => <Https height="50" width="50" />;
export const CustomStyle = () => <Https style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Https className="custom-class" />;
