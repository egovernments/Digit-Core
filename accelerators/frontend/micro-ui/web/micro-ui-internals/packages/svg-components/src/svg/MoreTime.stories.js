import React from "react";
import { MoreTime } from "./MoreTime";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "MoreTime",
  component: MoreTime,
};

export const Default = () => <MoreTime />;
export const Fill = () => <MoreTime fill="blue" />;
export const Size = () => <MoreTime height="50" width="50" />;
export const CustomStyle = () => <MoreTime style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <MoreTime className="custom-class" />;
