import React from "react";
import { AlternateEmail } from "./AlternateEmail";

export default {
  title: "AlternateEmail",
  component: AlternateEmail,
};

export const Default = () => <AlternateEmail />;
export const Fill = () => <AlternateEmail fill="blue" />;
export const Size = () => <AlternateEmail height="50" width="50" />;
export const CustomStyle = () => <AlternateEmail style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AlternateEmail className="custom-class" />;
