import React from "react";
import { Cancel } from "./Cancel";

export default {
  title: "Cancel",
  component: Cancel,
};

export const Default = () => <Cancel />;
export const Fill = () => <Cancel fill="blue" />;
export const Size = () => <Cancel height="50" width="50" />;
export const CustomStyle = () => <Cancel style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Cancel className="custom-class" />;
