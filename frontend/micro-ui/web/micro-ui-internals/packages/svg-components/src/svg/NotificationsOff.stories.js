import React from "react";
import { NotificationsOff } from "./NotificationsOff";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "NotificationsOff",
  component: NotificationsOff,
};

export const Default = () => <NotificationsOff />;
export const Fill = () => <NotificationsOff fill="blue" />;
export const Size = () => <NotificationsOff height="50" width="50" />;
export const CustomStyle = () => <NotificationsOff style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <NotificationsOff className="custom-class" />;
