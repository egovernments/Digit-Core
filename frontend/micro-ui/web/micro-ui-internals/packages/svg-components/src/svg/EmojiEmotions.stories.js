import React from "react";
import { EmojiEmotions } from "./EmojiEmotions";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "EmojiEmotions",
  component: EmojiEmotions,
};

export const Default = () => <EmojiEmotions />;
export const Fill = () => <EmojiEmotions fill="blue" />;
export const Size = () => <EmojiEmotions height="50" width="50" />;
export const CustomStyle = () => <EmojiEmotions style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <EmojiEmotions className="custom-class" />;

export const Clickable = () => <EmojiEmotions onClick={()=>console.log("clicked")} />;

const Template = (args) => <EmojiEmotions {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
