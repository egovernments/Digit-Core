import React from "react";
import { SettingsInputHdmi } from "./SettingsInputHdmi";

export default {
  title: "SettingsInputHdmi",
  component: SettingsInputHdmi,
};

export const Default = () => <SettingsInputHdmi />;
export const Fill = () => <SettingsInputHdmi fill="blue" />;
export const Size = () => <SettingsInputHdmi height="50" width="50" />;
export const CustomStyle = () => <SettingsInputHdmi style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SettingsInputHdmi className="custom-class" />;
