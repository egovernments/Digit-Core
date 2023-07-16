import React from "react";
import { Timeline } from "./Timeline";

export default {
  title: "Timeline",
  component: Timeline,
};

export const Default = () => <Timeline />;
export const Fill = () => <Timeline fill="blue" />;
export const Size = () => <Timeline height="50" width="50" />;
export const CustomStyle = () => <Timeline style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Timeline className="custom-class" />;
