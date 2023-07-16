import React from "react";
import { BugReport } from "./BugReport";

export default {
  title: "BugReport",
  component: BugReport,
};

export const Default = () => <BugReport />;
export const Fill = () => <BugReport fill="blue" />;
export const Size = () => <BugReport height="50" width="50" />;
export const CustomStyle = () => <BugReport style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <BugReport className="custom-class" />;
