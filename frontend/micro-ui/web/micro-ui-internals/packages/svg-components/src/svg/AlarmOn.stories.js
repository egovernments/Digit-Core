import React from "react";
import { AlarmOn } from "./AlarmOn";

export default {
  title: "AlarmOn",
  component: AlarmOn,
};

export const Default = () => <AlarmOn />;
export const Fill = () => <AlarmOn fill="blue" />;
export const Size = () => <AlarmOn height="50" width="50" />;
export const CustomStyle = () => <AlarmOn style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AlarmOn className="custom-class" />;
