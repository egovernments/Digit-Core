import React from "react";
import { Feedback } from "./Feedback";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Feedback",
  component: Feedback,
};

export const Default = () => <Feedback />;
export const Fill = () => <Feedback fill="blue" />;
export const Size = () => <Feedback height="50" width="50" />;
export const CustomStyle = () => <Feedback style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Feedback className="custom-class" />;
