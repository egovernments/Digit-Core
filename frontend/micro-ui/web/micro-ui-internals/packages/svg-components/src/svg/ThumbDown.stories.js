import React from "react";
import { ThumbDown } from "./ThumbDown";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ThumbDown",
  component: ThumbDown,
};

export const Default = () => <ThumbDown />;
export const Fill = () => <ThumbDown fill="blue" />;
export const Size = () => <ThumbDown height="50" width="50" />;
export const CustomStyle = () => <ThumbDown style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ThumbDown className="custom-class" />;
export const Clickable = () => <ThumbDown onClick={()=>console.log("clicked")} />;

const Template = (args) => <ThumbDown {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
