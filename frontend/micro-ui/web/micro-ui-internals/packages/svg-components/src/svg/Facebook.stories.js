import React from "react";
import { Facebook } from "./Facebook";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Facebook",
  component: Facebook,
};

export const Default = () => <Facebook />;
export const Fill = () => <Facebook fill="blue" />;
export const Size = () => <Facebook height="50" width="50" />;
export const CustomStyle = () => <Facebook style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Facebook className="custom-class" />;
