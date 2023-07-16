import React from "react";
import { NoTransfer } from "./NoTransfer";

export default {
  title: "NoTransfer",
  component: NoTransfer,
};

export const Default = () => <NoTransfer />;
export const Fill = () => <NoTransfer fill="blue" />;
export const Size = () => <NoTransfer height="50" width="50" />;
export const CustomStyle = () => <NoTransfer style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <NoTransfer className="custom-class" />;
