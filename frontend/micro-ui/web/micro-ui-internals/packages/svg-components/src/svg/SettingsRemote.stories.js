import React from "react";
import { SettingsRemote } from "./SettingsRemote";

export default {
  title: "SettingsRemote",
  component: SettingsRemote,
};

export const Default = () => <SettingsRemote />;
export const Fill = () => <SettingsRemote fill="blue" />;
export const Size = () => <SettingsRemote height="50" width="50" />;
export const CustomStyle = () => <SettingsRemote style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SettingsRemote className="custom-class" />;
