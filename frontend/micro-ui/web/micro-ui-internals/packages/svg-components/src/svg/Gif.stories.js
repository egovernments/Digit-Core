import React from "react";
import { Gif } from "./Gif";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Gif",
  component: Gif,
};

export const Default = () => <Gif />;
export const Fill = () => <Gif fill="blue" />;
export const Size = () => <Gif height="50" width="50" />;
export const CustomStyle = () => <Gif style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Gif className="custom-class" />;

export const Clickable = () => <Gif onClick={()=>console.log("clicked")} />;

const Template = (args) => <Gif {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
