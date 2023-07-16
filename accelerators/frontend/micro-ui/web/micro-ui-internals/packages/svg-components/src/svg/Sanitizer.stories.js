import React from "react";
import { Sanitizer } from "./Sanitizer";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Sanitizer",
  component: Sanitizer,
};

export const Default = () => <Sanitizer />;
export const Fill = () => <Sanitizer fill="blue" />;
export const Size = () => <Sanitizer height="50" width="50" />;
export const CustomStyle = () => <Sanitizer style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Sanitizer className="custom-class" />;
