import React from "react";
import { ThumbUpAlt } from "./ThumbUpAlt";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ThumbUpAlt",
  component: ThumbUpAlt,
};

export const Default = () => <ThumbUpAlt />;
export const Fill = () => <ThumbUpAlt fill="blue" />;
export const Size = () => <ThumbUpAlt height="50" width="50" />;
export const CustomStyle = () => <ThumbUpAlt style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ThumbUpAlt className="custom-class" />;
export const Clickable = () => <ThumbUpAlt onClick={()=>console.log("clicked")} />;

const Template = (args) => <ThumbUpAlt {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
