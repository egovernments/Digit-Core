import React from "react";
import { Forum } from "./Forum";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Forum",
  component: Forum,
};

export const Default = () => <Forum />;
export const Fill = () => <Forum fill="blue" />;
export const Size = () => <Forum height="50" width="50" />;
export const CustomStyle = () => <Forum style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Forum className="custom-class" />;
