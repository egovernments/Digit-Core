import React from "react";
import { ScreenShare } from "./ScreenShare";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ScreenShare",
  component: ScreenShare,
};

export const Default = () => <ScreenShare />;
export const Fill = () => <ScreenShare fill="blue" />;
export const Size = () => <ScreenShare height="50" width="50" />;
export const CustomStyle = () => <ScreenShare style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ScreenShare className="custom-class" />;
