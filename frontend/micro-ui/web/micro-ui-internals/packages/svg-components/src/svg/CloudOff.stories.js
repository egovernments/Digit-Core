import React from "react";
import { CloudOff } from "./CloudOff";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "CloudOff",
  component: CloudOff,
};

export const Default = () => <CloudOff />;
export const Fill = () => <CloudOff fill="blue" />;
export const Size = () => <CloudOff height="50" width="50" />;
export const CustomStyle = () => <CloudOff style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <CloudOff className="custom-class" />;

export const Clickable = () => <CloudOff onClick={()=>console.log("clicked")} />;

const Template = (args) => <CloudOff {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
