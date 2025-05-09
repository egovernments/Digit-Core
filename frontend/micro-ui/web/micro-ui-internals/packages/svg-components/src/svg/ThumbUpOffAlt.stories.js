import React from "react";
import { ThumbUpOffAlt } from "./ThumbUpOffAlt";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ThumbUpOffAlt",
  component: ThumbUpOffAlt,
};

export const Default = () => <ThumbUpOffAlt />;
export const Fill = () => <ThumbUpOffAlt fill="blue" />;
export const Size = () => <ThumbUpOffAlt height="50" width="50" />;
export const CustomStyle = () => <ThumbUpOffAlt style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ThumbUpOffAlt className="custom-class" />;
export const Clickable = () => <ThumbUpOffAlt onClick={()=>console.log("clicked")} />;

const Template = (args) => <ThumbUpOffAlt {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
