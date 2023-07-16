import React from "react";
import { AlarmOff } from "./AlarmOff";

export default {
  title: "AlarmOff",
  component: AlarmOff,
};

export const Default = () => <AlarmOff />;
export const Fill = () => <AlarmOff fill="blue" />;
export const Size = () => <AlarmOff height="50" width="50" />;
export const CustomStyle = () => <AlarmOff style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AlarmOff className="custom-class" />;
