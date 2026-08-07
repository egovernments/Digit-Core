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

export const Clickable = () => <EmojiFlags onClick={()=>console.log("clicked")} />;

const Template = (args) => <EmojiFlags {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
