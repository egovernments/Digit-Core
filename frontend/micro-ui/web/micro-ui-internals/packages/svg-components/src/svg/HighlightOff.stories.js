import React from "react";
import { HighlightOff } from "./HighlightOff";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "HighlightOff",
  component: HighlightOff,
};

export const Default = () => <HighlightOff />;
export const Fill = () => <HighlightOff fill="blue" />;
export const Size = () => <HighlightOff height="50" width="50" />;
export const CustomStyle = () => <HighlightOff style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <HighlightOff className="custom-class" />;
