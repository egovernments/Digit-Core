import React from "react";
import { SettingsCell } from "./SettingsCell";

export default {
  title: "SettingsCell",
  component: SettingsCell,
};

export const Default = () => <SettingsCell />;
export const Fill = () => <SettingsCell fill="blue" />;
export const Size = () => <SettingsCell height="50" width="50" />;
export const CustomStyle = () => <SettingsCell style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SettingsCell className="custom-class" />;
