import React from "react";
import { Engineering } from "./Engineering";

export default {
  title: "Engineering",
  component: Engineering,
};

export const Default = () => <Engineering />;
export const Fill = () => <Engineering fill="blue" />;
export const Size = () => <Engineering height="50" width="50" />;
export const CustomStyle = () => <Engineering style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Engineering className="custom-class" />;
