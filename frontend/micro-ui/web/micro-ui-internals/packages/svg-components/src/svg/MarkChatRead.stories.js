import React from "react";
import { MarkChatRead } from "./MarkChatRead";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "MarkChatRead",
  component: MarkChatRead,
};

export const Default = () => <MarkChatRead />;
export const Fill = () => <MarkChatRead fill="blue" />;
export const Size = () => <MarkChatRead height="50" width="50" />;
export const CustomStyle = () => <MarkChatRead style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <MarkChatRead className="custom-class" />;

export const Clickable = () => <MarkChatRead onClick={()=>console.log("clicked")} />;

const Template = (args) => <MarkChatRead {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
