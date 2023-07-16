import React from "react";
import { Alarm } from "./Alarm";

export default {
  title: "Alarm",
  component: Alarm,
};

export const Default = () => <Alarm />;
export const Fill = () => <Alarm fill="blue" />;
export const Size = () => <Alarm height="50" width="50" />;
export const CustomStyle = () => <Alarm style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Alarm className="custom-class" />;
