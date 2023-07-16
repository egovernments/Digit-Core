import React from "react";
import { AppBlocking } from "./AppBlocking";

export default {
  title: "AppBlocking",
  component: AppBlocking,
};

export const Default = () => <AppBlocking />;
export const Fill = () => <AppBlocking fill="blue" />;
export const Size = () => <AppBlocking height="50" width="50" />;
export const CustomStyle = () => <AppBlocking style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AppBlocking className="custom-class" />;
