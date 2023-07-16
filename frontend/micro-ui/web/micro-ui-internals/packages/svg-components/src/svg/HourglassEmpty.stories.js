import React from "react";
import { HourglassEmpty } from "./HourglassEmpty";

export default {
  title: "HourglassEmpty",
  component: HourglassEmpty,
};

export const Default = () => <HourglassEmpty />;
export const Fill = () => <HourglassEmpty fill="blue" />;
export const Size = () => <HourglassEmpty height="50" width="50" />;
export const CustomStyle = () => <HourglassEmpty style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <HourglassEmpty className="custom-class" />;
