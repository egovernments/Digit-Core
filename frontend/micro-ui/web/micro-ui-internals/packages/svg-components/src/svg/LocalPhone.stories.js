import React from "react";
import { LocalPhone } from "./LocalPhone";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LocalPhone",
  component: LocalPhone,
};

export const Default = () => <LocalPhone />;
export const Fill = () => <LocalPhone fill="blue" />;
export const Size = () => <LocalPhone height="50" width="50" />;
export const CustomStyle = () => <LocalPhone style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LocalPhone className="custom-class" />;

export const Clickable = () => <LocalPhone onClick={()=>console.log("clicked")} />;

const Template = (args) => <LocalPhone {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
