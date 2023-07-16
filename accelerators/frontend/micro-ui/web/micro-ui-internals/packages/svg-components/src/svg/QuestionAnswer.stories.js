import React from "react";
import { QuestionAnswer } from "./QuestionAnswer";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "QuestionAnswer",
  component: QuestionAnswer,
};

export const Default = () => <QuestionAnswer />;
export const Fill = () => <QuestionAnswer fill="blue" />;
export const Size = () => <QuestionAnswer height="50" width="50" />;
export const CustomStyle = () => <QuestionAnswer style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <QuestionAnswer className="custom-class" />;
