import React from "react";
import { CallReceived } from "./CallReceived";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "CallReceived",
  component: CallReceived,
};

export const Default = () => <CallReceived />;
export const Fill = () => <CallReceived fill="blue" />;
export const Size = () => <CallReceived height="50" width="50" />;
export const CustomStyle = () => <CallReceived style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <CallReceived className="custom-class" />;
