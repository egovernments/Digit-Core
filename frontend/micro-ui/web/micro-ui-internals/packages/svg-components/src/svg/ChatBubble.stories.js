import React from "react";
import { ChatBubble } from "./ChatBubble";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ChatBubble",
  component: ChatBubble,
};

export const Default = () => <ChatBubble />;
export const Fill = () => <ChatBubble fill="blue" />;
export const Size = () => <ChatBubble height="50" width="50" />;
export const CustomStyle = () => <ChatBubble style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ChatBubble className="custom-class" />;

export const Clickable = () => <ChatBubble onClick={()=>console.log("clicked")} />;

const Template = (args) => <ChatBubble {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
