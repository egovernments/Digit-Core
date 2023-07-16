import React from "react";
import { SystemUpdateAlt } from "./SystemUpdateAlt";

export default {
  title: "SystemUpdateAlt",
  component: SystemUpdateAlt,
};

export const Default = () => <SystemUpdateAlt />;
export const Fill = () => <SystemUpdateAlt fill="blue" />;
export const Size = () => <SystemUpdateAlt height="50" width="50" />;
export const CustomStyle = () => <SystemUpdateAlt style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SystemUpdateAlt className="custom-class" />;
