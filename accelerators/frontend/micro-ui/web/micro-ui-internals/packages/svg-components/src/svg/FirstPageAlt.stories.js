import React from "react";
import { FirstPageAlt } from "./FirstPageAlt";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "FirstPageAlt",
  component: FirstPageAlt,
};

export const Default = () => <FirstPageAlt />;
export const Fill = () => <FirstPageAlt fill="blue" />;
export const Size = () => <FirstPageAlt height="50" width="50" />;
export const CustomStyle = () => <FirstPageAlt style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <FirstPageAlt className="custom-class" />;
