import React from "react";
import { LabelOff } from "./LabelOff";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LabelOff",
  component: LabelOff,
};

export const Default = () => <LabelOff />;
export const Fill = () => <LabelOff fill="blue" />;
export const Size = () => <LabelOff height="50" width="50" />;
export const CustomStyle = () => <LabelOff style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LabelOff className="custom-class" />;
