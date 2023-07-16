import React from "react";
import { HistoryEdu } from "./HistoryEdu";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "HistoryEdu",
  component: HistoryEdu,
};

export const Default = () => <HistoryEdu />;
export const Fill = () => <HistoryEdu fill="blue" />;
export const Size = () => <HistoryEdu height="50" width="50" />;
export const CustomStyle = () => <HistoryEdu style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <HistoryEdu className="custom-class" />;
