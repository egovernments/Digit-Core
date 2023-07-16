import React from "react";
import { Notifications } from "./Notifications";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Notifications",
  component: Notifications,
};

export const Default = () => <Notifications />;
export const Fill = () => <Notifications fill="blue" />;
export const Size = () => <Notifications height="50" width="50" />;
export const CustomStyle = () => <Notifications style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Notifications className="custom-class" />;
