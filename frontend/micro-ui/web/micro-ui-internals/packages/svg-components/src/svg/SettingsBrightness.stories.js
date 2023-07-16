import React from "react";
import { SettingsBrightness } from "./SettingsBrightness";

export default {
  title: "SettingsBrightness",
  component: SettingsBrightness,
};

export const Default = () => <SettingsBrightness />;
export const Fill = () => <SettingsBrightness fill="blue" />;
export const Size = () => <SettingsBrightness height="50" width="50" />;
export const CustomStyle = () => <SettingsBrightness style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SettingsBrightness className="custom-class" />;
