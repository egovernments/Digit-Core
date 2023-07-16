import React from "react";
import { Receipt } from "./Receipt";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Receipt",
  component: Receipt,
};

export const Default = () => <Receipt />;
export const Fill = () => <Receipt fill="blue" />;
export const Size = () => <Receipt height="50" width="50" />;
export const CustomStyle = () => <Receipt style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Receipt className="custom-class" />;
