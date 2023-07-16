import React from "react";
import { Apps } from "./Apps";

export default {
  title: "Apps",
  component: Apps,
};

export const Default = () => <Apps />;
export const Fill = () => <Apps fill="blue" />;
export const Size = () => <Apps height="50" width="50" />;
export const CustomStyle = () => <Apps style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Apps className="custom-class" />;
