import React from "react";
import { SettingsInputComposite } from "./SettingsInputComposite";

export default {
  title: "SettingsInputComposite",
  component: SettingsInputComposite,
};

export const Default = () => <SettingsInputComposite />;
export const Fill = () => <SettingsInputComposite fill="blue" />;
export const Size = () => <SettingsInputComposite height="50" width="50" />;
export const CustomStyle = () => <SettingsInputComposite style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SettingsInputComposite className="custom-class" />;
