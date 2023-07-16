import React from "react";
import { File } from "./File";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "File",
  component: File,
};

export const Default = () => <File />;
export const Fill = () => <File fill="blue" />;
export const Size = () => <File height="50" width="50" />;
export const CustomStyle = () => <File style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <File className="custom-class" />;
