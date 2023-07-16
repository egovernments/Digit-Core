import React from "react";
import { Clock } from "./Clock";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Clock",
  component: Clock,
};

export const Default = () => <Clock />;
export const Fill = () => <Clock fill="blue" />;
export const Size = () => <Clock height="50" width="50" />;
export const CustomStyle = () => <Clock style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Clock className="custom-class" />;
