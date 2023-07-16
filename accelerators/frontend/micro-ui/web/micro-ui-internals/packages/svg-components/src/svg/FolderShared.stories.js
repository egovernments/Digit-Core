import React from "react";
import { FolderShared } from "./FolderShared";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "FolderShared",
  component: FolderShared,
};

export const Default = () => <FolderShared />;
export const Fill = () => <FolderShared fill="blue" />;
export const Size = () => <FolderShared height="50" width="50" />;
export const CustomStyle = () => <FolderShared style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <FolderShared className="custom-class" />;
