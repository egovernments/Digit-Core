import React from "react";
import { Chat } from "./Chat";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Chat",
  component: Chat,
};

export const Default = () => <Chat />;
export const Fill = () => <Chat fill="blue" />;
export const Size = () => <Chat height="50" width="50" />;
export const CustomStyle = () => <Chat style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Chat className="custom-class" />;

export const Clickable = () => <Chat onClick={()=>console.log("clicked")} />;

const Template = (args) => <Chat {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
