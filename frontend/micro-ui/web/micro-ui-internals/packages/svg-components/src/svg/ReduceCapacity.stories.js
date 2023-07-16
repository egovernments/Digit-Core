import React from "react";
import { ReduceCapacity } from "./ReduceCapacity";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ReduceCapacity",
  component: ReduceCapacity,
};

export const Default = () => <ReduceCapacity />;
export const Fill = () => <ReduceCapacity fill="blue" />;
export const Size = () => <ReduceCapacity height="50" width="50" />;
export const CustomStyle = () => <ReduceCapacity style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ReduceCapacity className="custom-class" />;
