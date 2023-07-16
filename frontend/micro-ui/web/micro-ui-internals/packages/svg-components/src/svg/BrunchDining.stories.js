import React from "react";
import { BrunchDining } from "./BrunchDining";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "BrunchDining",
  component: BrunchDining,
};

export const Default = () => <BrunchDining />;
export const Fill = () => <BrunchDining fill="blue" />;
export const Size = () => <BrunchDining height="50" width="50" />;
export const CustomStyle = () => <BrunchDining style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <BrunchDining className="custom-class" />;
