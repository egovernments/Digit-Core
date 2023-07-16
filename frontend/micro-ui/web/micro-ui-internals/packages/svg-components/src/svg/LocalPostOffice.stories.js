import React from "react";
import { LocalPostOffice } from "./LocalPostOffice";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LocalPostOffice",
  component: LocalPostOffice,
};

export const Default = () => <LocalPostOffice />;
export const Fill = () => <LocalPostOffice fill="blue" />;
export const Size = () => <LocalPostOffice height="50" width="50" />;
export const CustomStyle = () => <LocalPostOffice style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LocalPostOffice className="custom-class" />;
