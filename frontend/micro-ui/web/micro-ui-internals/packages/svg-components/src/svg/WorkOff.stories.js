import React from "react";
import { WorkOff } from "./WorkOff";

export default {
  title: "WorkOff",
  component: WorkOff,
};

export const Default = () => <WorkOff />;
export const Fill = () => <WorkOff fill="blue" />;
export const Size = () => <WorkOff height="50" width="50" />;
export const CustomStyle = () => <WorkOff style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <WorkOff className="custom-class" />;
