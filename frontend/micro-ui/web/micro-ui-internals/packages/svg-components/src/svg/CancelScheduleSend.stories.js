import React from "react";
import { CancelScheduleSend } from "./CancelScheduleSend";

export default {
  title: "CancelScheduleSend",
  component: CancelScheduleSend,
};

export const Default = () => <CancelScheduleSend />;
export const Fill = () => <CancelScheduleSend fill="blue" />;
export const Size = () => <CancelScheduleSend height="50" width="50" />;
export const CustomStyle = () => <CancelScheduleSend style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <CancelScheduleSend className="custom-class" />;
