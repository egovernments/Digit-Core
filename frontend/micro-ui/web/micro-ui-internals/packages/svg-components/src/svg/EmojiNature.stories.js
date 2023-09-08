import React from "react";
import { EmojiNature } from "./EmojiNature";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "EmojiNature",
  component: EmojiNature,
};

export const Default = () => <EmojiNature />;
export const Fill = () => <EmojiNature fill="blue" />;
export const Size = () => <EmojiNature height="50" width="50" />;
export const CustomStyle = () => <EmojiNature style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <EmojiNature className="custom-class" />;

export const Clickable = () => <EmojiNature onClick={()=>console.log("clicked")} />;

const Template = (args) => <EmojiNature {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
