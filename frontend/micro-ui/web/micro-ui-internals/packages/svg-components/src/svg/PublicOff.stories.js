import React from "react";
import { PublicOff } from "./PublicOff";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PublicOff",
  component: PublicOff,
};

export const Default = () => <PublicOff />;
export const Fill = () => <PublicOff fill="blue" />;
export const Size = () => <PublicOff height="50" width="50" />;
export const CustomStyle = () => <PublicOff style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PublicOff className="custom-class" />;

export const Clickable = () => <PublicOff onClick={()=>console.log("clicked")} />;

const Template = (args) => <PublicOff {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
