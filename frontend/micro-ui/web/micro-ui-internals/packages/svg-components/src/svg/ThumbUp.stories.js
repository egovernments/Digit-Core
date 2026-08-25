import React from "react";
import { ThumbUp } from "./ThumbUp";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ThumbUp",
  component: ThumbUp,
};

export const Default = () => <ThumbUp />;
export const Fill = () => <ThumbUp fill="blue" />;
export const Size = () => <ThumbUp height="50" width="50" />;
export const CustomStyle = () => <ThumbUp style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ThumbUp className="custom-class" />;
export const Clickable = () => <ThumbUp onClick={()=>console.log("clicked")} />;

const Template = (args) => <ThumbUp {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
