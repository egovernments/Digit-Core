import React from "react";
import { Handyman } from "./Handyman";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Handyman",
  component: Handyman,
};

export const Default = () => <Handyman />;
export const Fill = () => <Handyman fill="blue" />;
export const Size = () => <Handyman height="50" width="50" />;
export const CustomStyle = () => <Handyman style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Handyman className="custom-class" />;
