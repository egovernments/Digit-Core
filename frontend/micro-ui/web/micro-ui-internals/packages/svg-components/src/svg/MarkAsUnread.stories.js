import React from "react";
import { MarkAsUnread } from "./MarkAsUnread";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "MarkAsUnread",
  component: MarkAsUnread,
};

export const Default = () => <MarkAsUnread />;
export const Fill = () => <MarkAsUnread fill="blue" />;
export const Size = () => <MarkAsUnread height="50" width="50" />;
export const CustomStyle = () => <MarkAsUnread style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <MarkAsUnread className="custom-class" />;
