import React from "react";
import { VoiceOverOff } from "./VoiceOverOff";

export default {
  title: "VoiceOverOff",
  component: VoiceOverOff,
};

export const Default = () => <VoiceOverOff />;
export const Fill = () => <VoiceOverOff fill="blue" />;
export const Size = () => <VoiceOverOff height="50" width="50" />;
export const CustomStyle = () => <VoiceOverOff style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <VoiceOverOff className="custom-class" />;
