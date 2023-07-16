import React from "react";
import { TwoWheeler } from "./TwoWheeler";

export default {
  title: "TwoWheeler",
  component: TwoWheeler,
};

export const Default = () => <TwoWheeler />;
export const Fill = () => <TwoWheeler fill="blue" />;
export const Size = () => <TwoWheeler height="50" width="50" />;
export const CustomStyle = () => <TwoWheeler style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TwoWheeler className="custom-class" />;
