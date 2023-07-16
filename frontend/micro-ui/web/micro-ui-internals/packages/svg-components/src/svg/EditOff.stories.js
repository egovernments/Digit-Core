import React from "react";
import { EditOff } from "./EditOff";

export default {
  title: "EditOff",
  component: EditOff,
};

export const Default = () => <EditOff />;
export const Fill = () => <EditOff fill="blue" />;
export const Size = () => <EditOff height="50" width="50" />;
export const CustomStyle = () => <EditOff style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <EditOff className="custom-class" />;
