import React from "react";
import { LunchDining } from "./LunchDining";

export default {
  title: "LunchDining",
  component: LunchDining,
};

export const Default = () => <LunchDining />;
export const Fill = () => <LunchDining fill="blue" />;
export const Size = () => <LunchDining height="50" width="50" />;
export const CustomStyle = () => <LunchDining style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LunchDining className="custom-class" />;
