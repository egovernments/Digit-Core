import React from "react";
import { BackupTable } from "./BackupTable";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "BackupTable",
  component: BackupTable,
};

export const Default = () => <BackupTable />;
export const Fill = () => <BackupTable fill="blue" />;
export const Size = () => <BackupTable height="50" width="50" />;
export const CustomStyle = () => <BackupTable style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <BackupTable className="custom-class" />;
