import React from "react";
import { LabelImportant } from "./LabelImportant";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LabelImportant",
  component: LabelImportant,
};

export const Default = () => <LabelImportant />;
export const Fill = () => <LabelImportant fill="blue" />;
export const Size = () => <LabelImportant height="50" width="50" />;
export const CustomStyle = () => <LabelImportant style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LabelImportant className="custom-class" />;
