import React from "react";
import { ChatBubbleOutline } from "./ChatBubbleOutline";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ChatBubbleOutline",
  component: ChatBubbleOutline,
};

export const Default = () => <ChatBubbleOutline />;
export const Fill = () => <ChatBubbleOutline fill="blue" />;
export const Size = () => <ChatBubbleOutline height="50" width="50" />;
export const CustomStyle = () => <ChatBubbleOutline style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ChatBubbleOutline className="custom-class" />;

export const Clickable = () => <ChatBubbleOutline onClick={()=>console.log("clicked")} />;

const Template = (args) => <ChatBubbleOutline {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
