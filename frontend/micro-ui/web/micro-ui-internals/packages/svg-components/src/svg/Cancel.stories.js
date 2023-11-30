import React from "react";
import { Cancel } from "./Cancel";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Cancel",
  component: Cancel,
};

export const Default = () => <Cancel />;
export const Fill = () => <Cancel fill="blue" />;
export const Size = () => <Cancel height="50" width="50" />;
export const CustomStyle = () => <Cancel style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Cancel className="custom-class" />;

export const Clickable = () => <Cancel onClick={()=>console.log("clicked")} />;

const Template = (args) => <Cancel {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
