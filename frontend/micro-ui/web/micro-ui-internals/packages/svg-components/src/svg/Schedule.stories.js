import React from "react";
import { Schedule } from "./Schedule";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Schedule",
  component: Schedule,
};

export const Default = () => <Schedule />;
export const Fill = () => <Schedule fill="blue" />;
export const Size = () => <Schedule height="50" width="50" />;
export const CustomStyle = () => <Schedule style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Schedule className="custom-class" />;
