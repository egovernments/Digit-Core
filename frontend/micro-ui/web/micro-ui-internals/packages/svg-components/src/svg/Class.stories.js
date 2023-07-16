import React from "react";
import { Class } from "./Class";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Class",
  component: Class,
};

export const Default = () => <Class />;
export const Fill = () => <Class fill="blue" />;
export const Size = () => <Class height="50" width="50" />;
export const CustomStyle = () => <Class style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Class className="custom-class" />;
