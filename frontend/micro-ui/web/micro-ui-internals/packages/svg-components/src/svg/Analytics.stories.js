import React from "react";
import { Analytics } from "./Analytics";

export default {
  title: "Analytics",
  component: Analytics,
};

export const Default = () => <Analytics />;
export const Fill = () => <Analytics fill="blue" />;
export const Size = () => <Analytics height="50" width="50" />;
export const CustomStyle = () => <Analytics style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Analytics className="custom-class" />;
