import React from "react";
import { Loyalty } from "./Loyalty";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Loyalty",
  component: Loyalty,
};

export const Default = () => <Loyalty />;
export const Fill = () => <Loyalty fill="blue" />;
export const Size = () => <Loyalty height="50" width="50" />;
export const CustomStyle = () => <Loyalty style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Loyalty className="custom-class" />;
