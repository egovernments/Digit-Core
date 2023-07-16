import React from "react";
import { LockOpen } from "./LockOpen";

export default {
  title: "LockOpen",
  component: LockOpen,
};

export const Default = () => <LockOpen />;
export const Fill = () => <LockOpen fill="blue" />;
export const Size = () => <LockOpen height="50" width="50" />;
export const CustomStyle = () => <LockOpen style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LockOpen className="custom-class" />;
