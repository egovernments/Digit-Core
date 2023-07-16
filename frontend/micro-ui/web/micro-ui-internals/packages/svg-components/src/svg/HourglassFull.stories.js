import React from "react";
import { HourglassFull } from "./HourglassFull";

export default {
  title: "HourglassFull",
  component: HourglassFull,
};

export const Default = () => <HourglassFull />;
export const Fill = () => <HourglassFull fill="blue" />;
export const Size = () => <HourglassFull height="50" width="50" />;
export const CustomStyle = () => <HourglassFull style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <HourglassFull className="custom-class" />;
