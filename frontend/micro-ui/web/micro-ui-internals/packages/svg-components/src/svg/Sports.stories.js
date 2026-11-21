import React from "react";
import { Sports } from "./Sports";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Sports",
  component: Sports,
};

export const Default = () => <Sports />;
export const Fill = () => <Sports fill="blue" />;
export const Size = () => <Sports height="50" width="50" />;
export const CustomStyle = () => <Sports style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Sports className="custom-class" />;

export const Clickable = () => <Sports onClick={()=>console.log("clicked")} />;

const Template = (args) => <Sports {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
