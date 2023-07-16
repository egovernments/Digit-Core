import React from "react";
import { SpellCheck } from "./SpellCheck";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SpellCheck",
  component: SpellCheck,
};

export const Default = () => <SpellCheck />;
export const Fill = () => <SpellCheck fill="blue" />;
export const Size = () => <SpellCheck height="50" width="50" />;
export const CustomStyle = () => <SpellCheck style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SpellCheck className="custom-class" />;
