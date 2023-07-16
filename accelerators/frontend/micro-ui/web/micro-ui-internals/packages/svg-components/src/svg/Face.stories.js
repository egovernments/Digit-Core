import React from "react";
import { Face } from "./Face";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Face",
  component: Face,
};

export const Default = () => <Face />;
export const Fill = () => <Face fill="blue" />;
export const Size = () => <Face height="50" width="50" />;
export const CustomStyle = () => <Face style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Face className="custom-class" />;
