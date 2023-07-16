import React from "react";
import { Delete } from "./Delete";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Delete",
  component: Delete,
};

export const Default = () => <Delete />;
export const Fill = () => <Delete fill="blue" />;
export const Size = () => <Delete height="50" width="50" />;
export const CustomStyle = () => <Delete style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Delete className="custom-class" />;
