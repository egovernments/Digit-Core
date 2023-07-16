import React from "react";
import { Language } from "./Language";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Language",
  component: Language,
};

export const Default = () => <Language />;
export const Fill = () => <Language fill="blue" />;
export const Size = () => <Language height="50" width="50" />;
export const CustomStyle = () => <Language style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Language className="custom-class" />;
