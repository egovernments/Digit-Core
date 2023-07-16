import React from "react";
import { FullscreenExit } from "./FullscreenExit";

export default {
  title: "FullscreenExit",
  component: FullscreenExit,
};

export const Default = () => <FullscreenExit />;
export const Fill = () => <FullscreenExit fill="blue" />;
export const Size = () => <FullscreenExit height="50" width="50" />;
export const CustomStyle = () => <FullscreenExit style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <FullscreenExit className="custom-class" />;
