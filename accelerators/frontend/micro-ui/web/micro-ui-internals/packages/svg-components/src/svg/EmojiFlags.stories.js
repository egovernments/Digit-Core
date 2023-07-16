import React from "react";
import { EmojiFlags } from "./EmojiFlags";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "EmojiFlags",
  component: EmojiFlags,
};

export const Default = () => <EmojiFlags />;
export const Fill = () => <EmojiFlags fill="blue" />;
export const Size = () => <EmojiFlags height="50" width="50" />;
export const CustomStyle = () => <EmojiFlags style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <EmojiFlags className="custom-class" />;
