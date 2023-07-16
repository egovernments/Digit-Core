import React from "react";
import { CallSplit } from "./CallSplit";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "CallSplit",
  component: CallSplit,
};

export const Default = () => <CallSplit />;
export const Fill = () => <CallSplit fill="blue" />;
export const Size = () => <CallSplit height="50" width="50" />;
export const CustomStyle = () => <CallSplit style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <CallSplit className="custom-class" />;
