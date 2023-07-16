import React from "react";
import { Outbond } from "./Outbond";

export default {
  title: "Outbond",
  component: Outbond,
};

export const Default = () => <Outbond />;
export const Fill = () => <Outbond fill="blue" />;
export const Size = () => <Outbond height="50" width="50" />;
export const CustomStyle = () => <Outbond style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Outbond className="custom-class" />;
