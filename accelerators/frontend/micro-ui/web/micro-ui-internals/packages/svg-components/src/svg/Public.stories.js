import React from "react";
import { Public } from "./Public";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Public",
  component: Public,
};

export const Default = () => <Public />;
export const Fill = () => <Public fill="blue" />;
export const Size = () => <Public height="50" width="50" />;
export const CustomStyle = () => <Public style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Public className="custom-class" />;
