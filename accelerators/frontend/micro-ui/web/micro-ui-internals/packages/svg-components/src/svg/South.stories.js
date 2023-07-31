import React from "react";
import { South } from "./South";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "South",
  component: South,
};

export const Default = () => <South />;
export const Fill = () => <South fill="blue" />;
export const Size = () => <South height="50" width="50" />;
export const CustomStyle = () => <South style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <South className="custom-class" />;

export const Clickable = () => <South onClick={()=>console.log("clicked")} />;

const Template = (args) => <South {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
