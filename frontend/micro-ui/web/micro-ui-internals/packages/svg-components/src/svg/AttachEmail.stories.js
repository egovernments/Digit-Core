import React from "react";
import { AttachEmail } from "./AttachEmail";

export default {
  title: "AttachEmail",
  component: AttachEmail,
};

export const Default = () => <AttachEmail />;
export const Fill = () => <AttachEmail fill="blue" />;
export const Size = () => <AttachEmail height="50" width="50" />;
export const CustomStyle = () => <AttachEmail style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AttachEmail className="custom-class" />;
