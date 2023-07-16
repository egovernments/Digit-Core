import React from "react";
import { DonutLarge } from "./DonutLarge";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "DonutLarge",
  component: DonutLarge,
};

export const Default = () => <DonutLarge />;
export const Fill = () => <DonutLarge fill="blue" />;
export const Size = () => <DonutLarge height="50" width="50" />;
export const CustomStyle = () => <DonutLarge style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <DonutLarge className="custom-class" />;
