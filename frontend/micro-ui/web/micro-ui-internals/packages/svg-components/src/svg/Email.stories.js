import React from "react";
import { Email } from "./Email";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Email",
  component: Email,
};

export const Default = () => <Email />;
export const Fill = () => <Email fill="blue" />;
export const Size = () => <Email height="50" width="50" />;
export const CustomStyle = () => <Email style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Email className="custom-class" />;

export const Clickable = () => <Email onClick={()=>console.log("clicked")} />;

const Template = (args) => <Email {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
