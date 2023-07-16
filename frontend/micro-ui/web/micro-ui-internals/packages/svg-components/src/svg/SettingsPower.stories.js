import React from "react";
import { SettingsPower } from "./SettingsPower";

export default {
  title: "SettingsPower",
  component: SettingsPower,
};

export const Default = () => <SettingsPower />;
export const Fill = () => <SettingsPower fill="blue" />;
export const Size = () => <SettingsPower height="50" width="50" />;
export const CustomStyle = () => <SettingsPower style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SettingsPower className="custom-class" />;
