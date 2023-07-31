import React from "react";
import { MailOutline } from "./MailOutline";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "MailOutline",
  component: MailOutline,
};

export const Default = () => <MailOutline />;
export const Fill = () => <MailOutline fill="blue" />;
export const Size = () => <MailOutline height="50" width="50" />;
export const CustomStyle = () => <MailOutline style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <MailOutline className="custom-class" />;

export const Clickable = () => <MailOutline onClick={()=>console.log("clicked")} />;

const Template = (args) => <MailOutline {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
