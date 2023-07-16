import React from "react";
import { AlarmAdd } from "./AlarmAdd";

export default {
  title: "AlarmAdd",
  component: AlarmAdd,
};

export const Default = () => <AlarmAdd />;
export const Fill = () => <AlarmAdd fill="blue" />;
export const Size = () => <AlarmAdd height="50" width="50" />;
export const CustomStyle = () => <AlarmAdd style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AlarmAdd className="custom-class" />;
