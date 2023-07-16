import React from "react";
import { AccessibilityNew } from "./AccessibilityNew";

export default {
  title: "AccessibilityNew",
  component: AccessibilityNew,
};

export const Default = () => <AccessibilityNew />;
export const Fill = () => <AccessibilityNew fill="blue" />;
export const Size = () => <AccessibilityNew height="50" width="50" />;
export const CustomStyle = () => <AccessibilityNew style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AccessibilityNew className="custom-class" />;
