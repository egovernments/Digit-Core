import React from "react";
import { Settings } from "./Settings";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Settings",
  component: Settings,
};

export const Default = () => <Settings />;
export const Fill = () => <Settings fill="blue" />;
export const Size = () => <Settings height="50" width="50" />;
export const CustomStyle = () => <Settings style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Settings className="custom-class" />;
