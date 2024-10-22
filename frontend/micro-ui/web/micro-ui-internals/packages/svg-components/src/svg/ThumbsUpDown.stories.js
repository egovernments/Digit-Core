import React from "react";
import { ThumbsUpDown } from "./ThumbsUpDown";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ThumbsUpDown",
  component: ThumbsUpDown,
};

export const Default = () => <ThumbsUpDown />;
export const Fill = () => <ThumbsUpDown fill="blue" />;
export const Size = () => <ThumbsUpDown height="50" width="50" />;
export const CustomStyle = () => <ThumbsUpDown style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ThumbsUpDown className="custom-class" />;
export const Clickable = () => <ThumbsUpDown onClick={()=>console.log("clicked")} />;

const Template = (args) => <ThumbsUpDown {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
