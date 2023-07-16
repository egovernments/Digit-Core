import React from "react";
import { Cached } from "./Cached";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Cached",
  component: Cached,
};

export const Default = () => <Cached />;
export const Fill = () => <Cached fill="blue" />;
export const Size = () => <Cached height="50" width="50" />;
export const CustomStyle = () => <Cached style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Cached className="custom-class" />;
