import React from "react";
import { MarkEmailUnread } from "./MarkEmailUnread";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "MarkEmailUnread",
  component: MarkEmailUnread,
};

export const Default = () => <MarkEmailUnread />;
export const Fill = () => <MarkEmailUnread fill="blue" />;
export const Size = () => <MarkEmailUnread height="50" width="50" />;
export const CustomStyle = () => <MarkEmailUnread style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <MarkEmailUnread className="custom-class" />;

export const Clickable = () => <MarkEmailUnread onClick={()=>console.log("clicked")} />;

const Template = (args) => <MarkEmailUnread {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
