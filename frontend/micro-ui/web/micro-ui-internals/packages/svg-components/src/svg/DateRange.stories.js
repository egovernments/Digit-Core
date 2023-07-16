import React from "react";
import { DateRange } from "./DateRange";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "DateRange",
  component: DateRange,
};

export const Default = () => <DateRange />;
export const Fill = () => <DateRange fill="blue" />;
export const Size = () => <DateRange height="50" width="50" />;
export const CustomStyle = () => <DateRange style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <DateRange className="custom-class" />;
