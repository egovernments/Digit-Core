import React from "react";
import { ScreenShareStop } from "./ScreenShareStop";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ScreenShareStop",
  component: ScreenShareStop,
};

export const Default = () => <ScreenShareStop />;
export const Fill = () => <ScreenShareStop fill="blue" />;
export const Size = () => <ScreenShareStop height="50" width="50" />;
export const CustomStyle = () => <ScreenShareStop style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ScreenShareStop className="custom-class" />;
