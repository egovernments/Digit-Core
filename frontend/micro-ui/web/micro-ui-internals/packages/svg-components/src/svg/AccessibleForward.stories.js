import React from "react";
import { AccessibleForward } from "./AccessibleForward";

export default {
  title: "AccessibleForward",
  component: AccessibleForward,
};

export const Default = () => <AccessibleForward />;
export const Fill = () => <AccessibleForward fill="blue" />;
export const Size = () => <AccessibleForward height="50" width="50" />;
export const CustomStyle = () => <AccessibleForward style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AccessibleForward className="custom-class" />;
