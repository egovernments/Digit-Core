import React from "react";
import { ScheduleSend } from "./ScheduleSend";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ScheduleSend",
  component: ScheduleSend,
};

export const Default = () => <ScheduleSend />;
export const Fill = () => <ScheduleSend fill="blue" />;
export const Size = () => <ScheduleSend height="50" width="50" />;
export const CustomStyle = () => <ScheduleSend style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ScheduleSend className="custom-class" />;
