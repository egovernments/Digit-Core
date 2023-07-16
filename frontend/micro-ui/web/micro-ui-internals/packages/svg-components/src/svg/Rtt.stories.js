import React from "react";
import { Rtt } from "./Rtt";

export default {
  title: "Rtt",
  component: Rtt,
};

export const Default = () => <Rtt />;
export const Fill = () => <Rtt fill="blue" />;
export const Size = () => <Rtt height="50" width="50" />;
export const CustomStyle = () => <Rtt style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Rtt className="custom-class" />;
