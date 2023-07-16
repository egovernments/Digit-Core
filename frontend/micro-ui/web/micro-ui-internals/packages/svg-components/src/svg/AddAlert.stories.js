import React from "react";
import { AddAlert } from "./AddAlert";

export default {
  title: "AddAlert",
  component: AddAlert,
};

export const Default = () => <AddAlert />;
export const Fill = () => <AddAlert fill="blue" />;
export const Size = () => <AddAlert height="50" width="50" />;
export const CustomStyle = () => <AddAlert style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AddAlert className="custom-class" />;
