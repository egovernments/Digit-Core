import React from "react";
import { Book } from "./Book";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Book",
  component: Book,
};

export const Default = () => <Book />;
export const Fill = () => <Book fill="blue" />;
export const Size = () => <Book height="50" width="50" />;
export const CustomStyle = () => <Book style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Book className="custom-class" />;
