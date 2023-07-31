import React from "react";
import { Celebration } from "./Celebration";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Celebration",
  component: Celebration,
};

export const Default = () => <Celebration />;
export const Fill = () => <Celebration fill="blue" />;
export const Size = () => <Celebration height="50" width="50" />;
export const CustomStyle = () => <Celebration style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Celebration className="custom-class" />;

export const Clickable = () => <Celebration onClick={()=>console.log("clicked")} />;

const Template = (args) => <Celebration {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
