import React from "react";
import { SouthEast } from "./SouthEast";

export default {
  title: "SouthEast",
  component: SouthEast,
};

export const Default = () => <SouthEast />;
export const Fill = () => <SouthEast fill="blue" />;
export const Size = () => <SouthEast height="50" width="50" />;
export const CustomStyle = () => <SouthEast style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SouthEast className="custom-class" />;
