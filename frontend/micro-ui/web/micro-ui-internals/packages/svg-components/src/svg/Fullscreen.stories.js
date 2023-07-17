import React from "react";
import { Fullscreen } from "./Fullscreen";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Fullscreen",
  component: Fullscreen,
};

export const Default = () => <Fullscreen />;
export const Fill = () => <Fullscreen fill="blue" />;
export const Size = () => <Fullscreen height="50" width="50" />;
export const CustomStyle = () => <Fullscreen style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Fullscreen className="custom-class" />;

export const Clickable = () => <Fullscreen onClick={()=>console.log("clicked")} />;

const Template = (args) => <Fullscreen {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
