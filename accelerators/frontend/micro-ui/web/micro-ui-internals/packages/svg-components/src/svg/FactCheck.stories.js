import React from "react";
import { FactCheck } from "./FactCheck";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "FactCheck",
  component: FactCheck,
};

export const Default = () => <FactCheck />;
export const Fill = () => <FactCheck fill="blue" />;
export const Size = () => <FactCheck height="50" width="50" />;
export const CustomStyle = () => <FactCheck style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <FactCheck className="custom-class" />;
