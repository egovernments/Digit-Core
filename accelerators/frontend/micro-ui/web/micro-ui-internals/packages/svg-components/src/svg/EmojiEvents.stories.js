import React from "react";
import { EmojiEvents } from "./EmojiEvents";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "EmojiEvents",
  component: EmojiEvents,
};

export const Default = () => <EmojiEvents />;
export const Fill = () => <EmojiEvents fill="blue" />;
export const Size = () => <EmojiEvents height="50" width="50" />;
export const CustomStyle = () => <EmojiEvents style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <EmojiEvents className="custom-class" />;
