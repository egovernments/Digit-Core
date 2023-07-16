import React from "react";
import { NotificationsNone } from "./NotificationsNone";

export default {
  title: "NotificationsNone",
  component: NotificationsNone,
};

export const Default = () => <NotificationsNone />;
export const Fill = () => <NotificationsNone fill="blue" />;
export const Size = () => <NotificationsNone height="50" width="50" />;
export const CustomStyle = () => <NotificationsNone style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <NotificationsNone className="custom-class" />;
