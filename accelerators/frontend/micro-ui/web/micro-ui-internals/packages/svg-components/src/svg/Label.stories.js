import React from "react";
import { Label } from "./Label";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Label",
  component: Label,
};

export const Default = () => <Label />;
export const Fill = () => <Label fill="blue" />;
export const Size = () => <Label height="50" width="50" />;
export const CustomStyle = () => <Label style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Label className="custom-class" />;
