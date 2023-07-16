import React from "react";
import { RoundedCorner } from "./RoundedCorner";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "RoundedCorner",
  component: RoundedCorner,
};

export const Default = () => <RoundedCorner />;
export const Fill = () => <RoundedCorner fill="blue" />;
export const Size = () => <RoundedCorner height="50" width="50" />;
export const CustomStyle = () => <RoundedCorner style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <RoundedCorner className="custom-class" />;
