import React from "react";
import { LocalPrintshop } from "./LocalPrintshop";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LocalPrintshop",
  component: LocalPrintshop,
};

export const Default = () => <LocalPrintshop />;
export const Fill = () => <LocalPrintshop fill="blue" />;
export const Size = () => <LocalPrintshop height="50" width="50" />;
export const CustomStyle = () => <LocalPrintshop style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LocalPrintshop className="custom-class" />;
