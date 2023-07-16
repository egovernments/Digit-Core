import React from "react";
import { AppSettingsAlt } from "./AppSettingsAlt";

export default {
  title: "AppSettingsAlt",
  component: AppSettingsAlt,
};

export const Default = () => <AppSettingsAlt />;
export const Fill = () => <AppSettingsAlt fill="blue" />;
export const Size = () => <AppSettingsAlt height="50" width="50" />;
export const CustomStyle = () => <AppSettingsAlt style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AppSettingsAlt className="custom-class" />;
