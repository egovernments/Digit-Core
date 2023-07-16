import React from "react";
import { ClearAll } from "./ClearAll";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ClearAll",
  component: ClearAll,
};

export const Default = () => <ClearAll />;
export const Fill = () => <ClearAll fill="blue" />;
export const Size = () => <ClearAll height="50" width="50" />;
export const CustomStyle = () => <ClearAll style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ClearAll className="custom-class" />;
