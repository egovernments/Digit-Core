import React from "react";
import { Facebook } from "./Facebook";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Facebook",
  component: Facebook,
};

export const Default = () => <Facebook />;
export const Fill = () => <Facebook fill="blue" />;
export const Size = () => <Facebook height="50" width="50" />;
export const CustomStyle = () => <Facebook style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Facebook className="custom-class" />;

export const Clickable = () => <Facebook onClick={()=>console.log("clicked")} />;

const Template = (args) => <Facebook {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
