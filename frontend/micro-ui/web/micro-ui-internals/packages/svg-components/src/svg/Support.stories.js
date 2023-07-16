import React from "react";
import { Support } from "./Support";

export default {
  title: "Support",
  component: Support,
};

export const Default = () => <Support />;
export const Fill = () => <Support fill="blue" />;
export const Size = () => <Support height="50" width="50" />;
export const CustomStyle = () => <Support style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Support className="custom-class" />;
