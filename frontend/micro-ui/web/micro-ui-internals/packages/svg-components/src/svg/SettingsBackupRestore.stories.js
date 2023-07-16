import React from "react";
import { SettingsBackupRestore } from "./SettingsBackupRestore";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SettingsBackupRestore",
  component: SettingsBackupRestore,
};

export const Default = () => <SettingsBackupRestore />;
export const Fill = () => <SettingsBackupRestore fill="blue" />;
export const Size = () => <SettingsBackupRestore height="50" width="50" />;
export const CustomStyle = () => <SettingsBackupRestore style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SettingsBackupRestore className="custom-class" />;
