import React from "react";
import { DesktopAccessDisabled } from "./DesktopAccessDisabled";

export default {
  title: "DesktopAccessDisabled",
  component: DesktopAccessDisabled,
};

export const Default = () => <DesktopAccessDisabled />;
export const Fill = () => <DesktopAccessDisabled fill="blue" />;
export const Size = () => <DesktopAccessDisabled height="50" width="50" />;
export const CustomStyle = () => <DesktopAccessDisabled style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <DesktopAccessDisabled className="custom-class" />;
