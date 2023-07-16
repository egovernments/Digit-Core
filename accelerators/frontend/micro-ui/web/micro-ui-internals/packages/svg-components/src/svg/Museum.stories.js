import React from "react";
import { Museum } from "./Museum";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Museum",
  component: Museum,
};

export const Default = () => <Museum />;
export const Fill = () => <Museum fill="blue" />;
export const Size = () => <Museum height="50" width="50" />;
export const CustomStyle = () => <Museum style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Museum className="custom-class" />;
