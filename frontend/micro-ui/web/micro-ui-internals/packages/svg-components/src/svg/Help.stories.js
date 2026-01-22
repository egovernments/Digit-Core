import React from "react";
import { Help } from "./Help";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Help",
  component: Help,
};

export const Default = () => <Help />;
export const Fill = () => <Help fill="blue" />;
export const Size = () => <Help height="50" width="50" />;
export const CustomStyle = () => <Help style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Help className="custom-class" />;

export const Clickable = () => <Help onClick={()=>console.log("clicked")} />;

const Template = (args) => <Help {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
