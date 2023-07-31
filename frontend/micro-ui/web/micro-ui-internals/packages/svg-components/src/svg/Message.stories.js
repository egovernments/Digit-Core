import React from "react";
import { Message } from "./Message";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Message",
  component: Message,
};

export const Default = () => <Message />;
export const Fill = () => <Message fill="blue" />;
export const Size = () => <Message height="50" width="50" />;
export const CustomStyle = () => <Message style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Message className="custom-class" />;

export const Clickable = () => <Message onClick={()=>console.log("clicked")} />;

const Template = (args) => <Message {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
