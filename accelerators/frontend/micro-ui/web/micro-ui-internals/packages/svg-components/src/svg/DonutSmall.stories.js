import React from "react";
import { DonutSmall } from "./DonutSmall";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "DonutSmall",
  component: DonutSmall,
};

export const Default = () => <DonutSmall />;
export const Fill = () => <DonutSmall fill="blue" />;
export const Size = () => <DonutSmall height="50" width="50" />;
export const CustomStyle = () => <DonutSmall style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <DonutSmall className="custom-class" />;
