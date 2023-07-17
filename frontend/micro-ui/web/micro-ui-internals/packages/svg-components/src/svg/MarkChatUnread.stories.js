import React from "react";
import { MarkChatUnread } from "./MarkChatUnread";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "MarkChatUnread",
  component: MarkChatUnread,
};

export const Default = () => <MarkChatUnread />;
export const Fill = () => <MarkChatUnread fill="blue" />;
export const Size = () => <MarkChatUnread height="50" width="50" />;
export const CustomStyle = () => <MarkChatUnread style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <MarkChatUnread className="custom-class" />;

export const Clickable = () => <MarkChatUnread onClick={()=>console.log("clicked")} />;

const Template = (args) => <MarkChatUnread {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
