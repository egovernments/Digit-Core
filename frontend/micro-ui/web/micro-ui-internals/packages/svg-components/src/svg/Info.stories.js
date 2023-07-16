import React from "react";
import { Info } from "./Info";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Info",
  component: Info,
};

export const Default = () => <Info />;
export const Fill = () => <Info fill="blue" />;
export const Size = () => <Info height="50" width="50" />;
export const CustomStyle = () => <Info style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Info className="custom-class" />;
