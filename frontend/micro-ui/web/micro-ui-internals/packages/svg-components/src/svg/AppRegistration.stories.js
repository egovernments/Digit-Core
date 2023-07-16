import React from "react";
import { AppRegistration } from "./AppRegistration";

export default {
  title: "AppRegistration",
  component: AppRegistration,
};

export const Default = () => <AppRegistration />;
export const Fill = () => <AppRegistration fill="blue" />;
export const Size = () => <AppRegistration height="50" width="50" />;
export const CustomStyle = () => <AppRegistration style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AppRegistration className="custom-class" />;
