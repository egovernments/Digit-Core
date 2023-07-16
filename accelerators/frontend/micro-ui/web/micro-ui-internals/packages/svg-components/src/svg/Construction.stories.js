import React from "react";
import { Construction } from "./Construction";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Construction",
  component: Construction,
};

export const Default = () => <Construction />;
export const Fill = () => <Construction fill="blue" />;
export const Size = () => <Construction height="50" width="50" />;
export const CustomStyle = () => <Construction style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Construction className="custom-class" />;
