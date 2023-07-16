import React from "react";
import { Call } from "./Call";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Call",
  component: Call,
};

export const Default = () => <Call />;
export const Fill = () => <Call fill="blue" />;
export const Size = () => <Call height="50" width="50" />;
export const CustomStyle = () => <Call style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Call className="custom-class" />;
