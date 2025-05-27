import React from "react";
import { ThumbDownAlt } from "./ThumbDownAlt";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ThumbDownAlt",
  component: ThumbDownAlt,
};

export const Default = () => <ThumbDownAlt />;
export const Fill = () => <ThumbDownAlt fill="blue" />;
export const Size = () => <ThumbDownAlt height="50" width="50" />;
export const CustomStyle = () => <ThumbDownAlt style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ThumbDownAlt className="custom-class" />;
export const Clickable = () => <ThumbDownAlt onClick={()=>console.log("clicked")} />;

const Template = (args) => <ThumbDownAlt {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
