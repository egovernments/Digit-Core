import React from "react";
import { RunCircle } from "./RunCircle";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "RunCircle",
  component: RunCircle,
};

export const Default = () => <RunCircle />;
export const Fill = () => <RunCircle fill="blue" />;
export const Size = () => <RunCircle height="50" width="50" />;
export const CustomStyle = () => <RunCircle style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <RunCircle className="custom-class" />;

export const Clickable = () => <RunCircle onClick={()=>console.log("clicked")} />;

const Template = (args) => <RunCircle {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
