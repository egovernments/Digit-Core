import React from "react";
import { PermPhoneMsg } from "./PermPhoneMsg";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PermPhoneMsg",
  component: PermPhoneMsg,
};

export const Default = () => <PermPhoneMsg />;
export const Fill = () => <PermPhoneMsg fill="blue" />;
export const Size = () => <PermPhoneMsg height="50" width="50" />;
export const CustomStyle = () => <PermPhoneMsg style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PermPhoneMsg className="custom-class" />;
