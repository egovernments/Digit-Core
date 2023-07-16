import React from "react";
import { SettingsVoice } from "./SettingsVoice";

export default {
  title: "SettingsVoice",
  component: SettingsVoice,
};

export const Default = () => <SettingsVoice />;
export const Fill = () => <SettingsVoice fill="blue" />;
export const Size = () => <SettingsVoice height="50" width="50" />;
export const CustomStyle = () => <SettingsVoice style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SettingsVoice className="custom-class" />;
