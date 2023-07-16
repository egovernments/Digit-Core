import React from "react";
import { ErrorOutline } from "./ErrorOutline";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ErrorOutline",
  component: ErrorOutline,
};

export const Default = () => <ErrorOutline />;
export const Fill = () => <ErrorOutline fill="blue" />;
export const Size = () => <ErrorOutline height="50" width="50" />;
export const CustomStyle = () => <ErrorOutline style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ErrorOutline className="custom-class" />;
