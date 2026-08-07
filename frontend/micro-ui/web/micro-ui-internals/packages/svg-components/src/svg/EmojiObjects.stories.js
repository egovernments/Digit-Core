import React from "react";
import { EmojiObjects } from "./EmojiObjects";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "EmojiObjects",
  component: EmojiObjects,
};

export const Default = () => <EmojiObjects />;
export const Fill = () => <EmojiObjects fill="blue" />;
export const Size = () => <EmojiObjects height="50" width="50" />;
export const CustomStyle = () => <EmojiObjects style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <EmojiObjects className="custom-class" />;

export const Clickable = () => <EmojiObjects onClick={()=>console.log("clicked")} />;

const Template = (args) => <EmojiObjects {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
