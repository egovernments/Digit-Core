import React from "react";
import { Poll } from "./Poll";

export default {
  title: "Poll",
  component: Poll,
};

export const Default = () => <Poll />;
export const Fill = () => <Poll fill="blue" />;
export const Size = () => <Poll height="50" width="50" />;
export const CustomStyle = () => <Poll style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Poll className="custom-class" />;
