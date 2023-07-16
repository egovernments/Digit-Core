import React from "react";
import { NorthWest } from "./NorthWest";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "NorthWest",
  component: NorthWest,
};

export const Default = () => <NorthWest />;
export const Fill = () => <NorthWest fill="blue" />;
export const Size = () => <NorthWest height="50" width="50" />;
export const CustomStyle = () => <NorthWest style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <NorthWest className="custom-class" />;
