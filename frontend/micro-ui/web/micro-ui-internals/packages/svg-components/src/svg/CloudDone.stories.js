import React from "react";
import { CloudDone } from "./CloudDone";

export default {
  title: "CloudDone",
  component: CloudDone,
};

export const Default = () => <CloudDone />;
export const Fill = () => <CloudDone fill="blue" />;
export const Size = () => <CloudDone height="50" width="50" />;
export const CustomStyle = () => <CloudDone style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <CloudDone className="custom-class" />;
