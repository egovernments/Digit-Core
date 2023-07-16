import React from "react";
import { InvertColorsOff } from "./InvertColorsOff";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "InvertColorsOff",
  component: InvertColorsOff,
};

export const Default = () => <InvertColorsOff />;
export const Fill = () => <InvertColorsOff fill="blue" />;
export const Size = () => <InvertColorsOff height="50" width="50" />;
export const CustomStyle = () => <InvertColorsOff style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <InvertColorsOff className="custom-class" />;
