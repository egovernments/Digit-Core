import React from "react";
import { Directions } from "./Directions";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Directions",
  component: Directions,
};

export const Default = () => <Directions />;
export const Fill = () => <Directions fill="blue" />;
export const Size = () => <Directions height="50" width="50" />;
export const CustomStyle = () => <Directions style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Directions className="custom-class" />;
