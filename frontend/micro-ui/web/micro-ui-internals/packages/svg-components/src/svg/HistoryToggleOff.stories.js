import React from "react";
import { HistoryToggleOff } from "./HistoryToggleOff";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "HistoryToggleOff",
  component: HistoryToggleOff,
};

export const Default = () => <HistoryToggleOff />;
export const Fill = () => <HistoryToggleOff fill="blue" />;
export const Size = () => <HistoryToggleOff height="50" width="50" />;
export const CustomStyle = () => <HistoryToggleOff style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <HistoryToggleOff className="custom-class" />;
