import React from "react";
import { Reorder } from "./Reorder";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Reorder",
  component: Reorder,
};

export const Default = () => <Reorder />;
export const Fill = () => <Reorder fill="blue" />;
export const Size = () => <Reorder height="50" width="50" />;
export const CustomStyle = () => <Reorder style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Reorder className="custom-class" />;
