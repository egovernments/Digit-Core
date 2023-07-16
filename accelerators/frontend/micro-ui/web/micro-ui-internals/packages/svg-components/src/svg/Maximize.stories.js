import React from "react";
import { Maximize } from "./Maximize";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Maximize",
  component: Maximize,
};

export const Default = () => <Maximize />;
export const Fill = () => <Maximize fill="blue" />;
export const Size = () => <Maximize height="50" width="50" />;
export const CustomStyle = () => <Maximize style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Maximize className="custom-class" />;
