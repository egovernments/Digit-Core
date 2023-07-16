import React from "react";
import { MarkChatUnread } from "./MarkChatUnread";

export default {
  title: "MarkChatUnread",
  component: MarkChatUnread,
};

export const Default = () => <MarkChatUnread />;
export const Fill = () => <MarkChatUnread fill="blue" />;
export const Size = () => <MarkChatUnread height="50" width="50" />;
export const CustomStyle = () => <MarkChatUnread style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <MarkChatUnread className="custom-class" />;
