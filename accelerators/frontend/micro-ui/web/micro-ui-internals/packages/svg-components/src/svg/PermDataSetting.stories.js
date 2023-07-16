import React from "react";
import { PermDataSetting } from "./PermDataSetting";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PermDataSetting",
  component: PermDataSetting,
};

export const Default = () => <PermDataSetting />;
export const Fill = () => <PermDataSetting fill="blue" />;
export const Size = () => <PermDataSetting height="50" width="50" />;
export const CustomStyle = () => <PermDataSetting style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PermDataSetting className="custom-class" />;
