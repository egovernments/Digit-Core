import React from "react";
import { Subject } from "./Subject";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Subject",
  component: Subject,
};

export const Default = () => <Subject />;
export const Fill = () => <Subject fill="blue" />;
export const Size = () => <Subject height="50" width="50" />;
export const CustomStyle = () => <Subject style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Subject className="custom-class" />;
