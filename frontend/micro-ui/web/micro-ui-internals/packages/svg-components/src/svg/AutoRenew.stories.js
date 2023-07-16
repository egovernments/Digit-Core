import React from "react";
import { AutoRenew } from "./AutoRenew";

export default {
  title: "AutoRenew",
  component: AutoRenew,
};

export const Default = () => <AutoRenew />;
export const Fill = () => <AutoRenew fill="blue" />;
export const Size = () => <AutoRenew height="50" width="50" />;
export const CustomStyle = () => <AutoRenew style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AutoRenew className="custom-class" />;
