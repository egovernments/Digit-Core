import React from "react";
import { Rule } from "./Rule";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Rule",
  component: Rule,
};

export const Default = () => <Rule />;
export const Fill = () => <Rule fill="blue" />;
export const Size = () => <Rule height="50" width="50" />;
export const CustomStyle = () => <Rule style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Rule className="custom-class" />;
