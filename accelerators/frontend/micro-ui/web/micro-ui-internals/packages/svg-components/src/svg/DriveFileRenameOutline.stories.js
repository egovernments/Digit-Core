import React from "react";
import { DriveFileRenameOutline } from "./DriveFileRenameOutline";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "DriveFileRenameOutline",
  component: DriveFileRenameOutline,
};

export const Default = () => <DriveFileRenameOutline />;
export const Fill = () => <DriveFileRenameOutline fill="blue" />;
export const Size = () => <DriveFileRenameOutline height="50" width="50" />;
export const CustomStyle = () => <DriveFileRenameOutline style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <DriveFileRenameOutline className="custom-class" />;
