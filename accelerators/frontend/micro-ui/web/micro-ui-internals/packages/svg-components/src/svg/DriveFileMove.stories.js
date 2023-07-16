import React from "react";
import { DriveFileMove } from "./DriveFileMove";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "DriveFileMove",
  component: DriveFileMove,
};

export const Default = () => <DriveFileMove />;
export const Fill = () => <DriveFileMove fill="blue" />;
export const Size = () => <DriveFileMove height="50" width="50" />;
export const CustomStyle = () => <DriveFileMove style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <DriveFileMove className="custom-class" />;
