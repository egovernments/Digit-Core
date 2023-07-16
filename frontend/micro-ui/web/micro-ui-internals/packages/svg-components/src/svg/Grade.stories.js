import React from "react";
import { Grade } from "./Grade";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Grade",
  component: Grade,
};

export const Default = () => <Grade />;
export const Fill = () => <Grade fill="blue" />;
export const Size = () => <Grade height="50" width="50" />;
export const CustomStyle = () => <Grade style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Grade className="custom-class" />;
