import React from "react";
import { IconSwerage } from "./IconSwerage";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "IconSwerage",
  component: IconSwerage,
};

export const Default = () => <IconSwerage />;
export const Fill = () => <IconSwerage fill="blue" />;
export const Size = () => <IconSwerage height="50" width="50" />;
export const CustomStyle = () => <IconSwerage style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <IconSwerage className="custom-class" />;
