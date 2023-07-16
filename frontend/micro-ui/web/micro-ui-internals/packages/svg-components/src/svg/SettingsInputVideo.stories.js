import React from "react";
import { SettingsInputVideo } from "./SettingsInputVideo";

export default {
  title: "SettingsInputVideo",
  component: SettingsInputVideo,
};

export const Default = () => <SettingsInputVideo />;
export const Fill = () => <SettingsInputVideo fill="blue" />;
export const Size = () => <SettingsInputVideo height="50" width="50" />;
export const CustomStyle = () => <SettingsInputVideo style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SettingsInputVideo className="custom-class" />;
