import React from "react";
import { Android } from "./Android";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Android",
  component: Android,
};

export const Default = () => <Android />;
export const Fill = () => <Android fill="blue" />;
export const Size = () => <Android height="50" width="50" />;
export const CustomStyle = () => <Android style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Android className="custom-class" />;
export const Clickable = () => <Android onClick={()=>console.log("clicked")} />;

const Template = (args) => <Android {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
