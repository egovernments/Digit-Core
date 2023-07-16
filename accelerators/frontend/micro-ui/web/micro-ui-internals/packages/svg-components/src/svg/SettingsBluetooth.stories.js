import React from "react";
import { SettingsBluetooth } from "./SettingsBluetooth";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SettingsBluetooth",
  component: SettingsBluetooth,
};

export const Default = () => <SettingsBluetooth />;
export const Fill = () => <SettingsBluetooth fill="blue" />;
export const Size = () => <SettingsBluetooth height="50" width="50" />;
export const CustomStyle = () => <SettingsBluetooth style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SettingsBluetooth className="custom-class" />;
