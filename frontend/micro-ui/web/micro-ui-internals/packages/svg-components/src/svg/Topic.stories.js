import React from "react";
import { Topic } from "./Topic";

export default {
  title: "Topic",
  component: Topic,
};

export const Default = () => <Topic />;
export const Fill = () => <Topic fill="blue" />;
export const Size = () => <Topic height="50" width="50" />;
export const CustomStyle = () => <Topic style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Topic className="custom-class" />;
