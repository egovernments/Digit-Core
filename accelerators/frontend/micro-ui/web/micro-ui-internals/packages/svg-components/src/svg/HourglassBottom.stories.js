import React from "react";
import { HourglassBottom } from "./HourglassBottom";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "HourglassBottom",
  component: HourglassBottom,
};

export const Default = () => <HourglassBottom />;
export const Fill = () => <HourglassBottom fill="blue" />;
export const Size = () => <HourglassBottom height="50" width="50" />;
export const CustomStyle = () => <HourglassBottom style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <HourglassBottom className="custom-class" />;
