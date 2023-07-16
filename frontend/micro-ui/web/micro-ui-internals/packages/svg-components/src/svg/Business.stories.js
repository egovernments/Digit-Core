import React from "react";
import { Business } from "./Business";

export default {
  title: "Business",
  component: Business,
};

export const Default = () => <Business />;
export const Fill = () => <Business fill="blue" />;
export const Size = () => <Business height="50" width="50" />;
export const CustomStyle = () => <Business style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Business className="custom-class" />;
