import React from "react";
import { Hardware } from "./Hardware";

export default {
  title: "Hardware",
  component: Hardware,
};

export const Default = () => <Hardware />;
export const Fill = () => <Hardware fill="blue" />;
export const Size = () => <Hardware height="50" width="50" />;
export const CustomStyle = () => <Hardware style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Hardware className="custom-class" />;
