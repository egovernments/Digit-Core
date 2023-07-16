import React from "react";
import { Done } from "./Done";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Done",
  component: Done,
};

export const Default = () => <Done />;
export const Fill = () => <Done fill="blue" />;
export const Size = () => <Done height="50" width="50" />;
export const CustomStyle = () => <Done style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Done className="custom-class" />;
