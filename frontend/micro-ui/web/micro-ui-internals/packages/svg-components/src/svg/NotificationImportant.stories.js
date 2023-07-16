import React from "react";
import { NotificationImportant } from "./NotificationImportant";

export default {
  title: "NotificationImportant",
  component: NotificationImportant,
};

export const Default = () => <NotificationImportant />;
export const Fill = () => <NotificationImportant fill="blue" />;
export const Size = () => <NotificationImportant height="50" width="50" />;
export const CustomStyle = () => <NotificationImportant style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <NotificationImportant className="custom-class" />;
