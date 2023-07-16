import React from "react";
import { PinDrop } from "./PinDrop";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PinDrop",
  component: PinDrop,
};

export const Default = () => <PinDrop />;
export const Fill = () => <PinDrop fill="blue" />;
export const Size = () => <PinDrop height="50" width="50" />;
export const CustomStyle = () => <PinDrop style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PinDrop className="custom-class" />;
