import React from "react";
import { Code } from "./Code";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Code",
  component: Code,
};

export const Default = () => <Code />;
export const Fill = () => <Code fill="blue" />;
export const Size = () => <Code height="50" width="50" />;
export const CustomStyle = () => <Code style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Code className="custom-class" />;

export const Clickable = () => <Code onClick={()=>console.log("clicked")} />;

const Template = (args) => <Code {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
