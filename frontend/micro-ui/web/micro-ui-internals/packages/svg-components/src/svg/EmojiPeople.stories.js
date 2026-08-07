import React from "react";
import { EmojiPeople } from "./EmojiPeople";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "EmojiPeople",
  component: EmojiPeople,
};

export const Default = () => <EmojiPeople />;
export const Fill = () => <EmojiPeople fill="blue" />;
export const Size = () => <EmojiPeople height="50" width="50" />;
export const CustomStyle = () => <EmojiPeople style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <EmojiPeople className="custom-class" />;

export const Clickable = () => <EmojiPeople onClick={()=>console.log("clicked")} />;

const Template = (args) => <EmojiPeople {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
