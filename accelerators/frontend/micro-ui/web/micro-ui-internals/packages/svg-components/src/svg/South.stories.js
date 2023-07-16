import React from "react";
import { South } from "./South";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "South",
  component: South,
};

export const Default = () => <South />;
export const Fill = () => <South fill="blue" />;
export const Size = () => <South height="50" width="50" />;
export const CustomStyle = () => <South style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <South className="custom-class" />;
