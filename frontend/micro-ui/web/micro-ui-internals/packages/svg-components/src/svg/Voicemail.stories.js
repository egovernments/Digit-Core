import React from "react";
import { Voicemail } from "./Voicemail";

export default {
  title: "Voicemail",
  component: Voicemail,
};

export const Default = () => <Voicemail />;
export const Fill = () => <Voicemail fill="blue" />;
export const Size = () => <Voicemail height="50" width="50" />;
export const CustomStyle = () => <Voicemail style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Voicemail className="custom-class" />;
