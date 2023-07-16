import React from "react";
import { CircleNotifications } from "./CircleNotifications";

export default {
  title: "CircleNotifications",
  component: CircleNotifications,
};

export const Default = () => <CircleNotifications />;
export const Fill = () => <CircleNotifications fill="blue" />;
export const Size = () => <CircleNotifications height="50" width="50" />;
export const CustomStyle = () => <CircleNotifications style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <CircleNotifications className="custom-class" />;
