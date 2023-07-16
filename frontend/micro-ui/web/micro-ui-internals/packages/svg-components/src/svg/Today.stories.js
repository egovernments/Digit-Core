import React from "react";
import { Today } from "./Today";

export default {
  title: "Today",
  component: Today,
};

export const Default = () => <Today />;
export const Fill = () => <Today fill="blue" />;
export const Size = () => <Today height="50" width="50" />;
export const CustomStyle = () => <Today style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Today className="custom-class" />;
