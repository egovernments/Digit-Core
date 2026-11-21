import React from "react";
import { Agriculture } from "./Agriculture";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Agriculture",
  component: Agriculture,
};

export const Default = () => <Agriculture />;
export const Fill = () => <Agriculture fill="blue" />;
export const Size = () => <Agriculture height="50" width="50" />;
export const CustomStyle = () => <Agriculture style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Agriculture className="custom-class" />;
export const Clickable = () => <Agriculture onClick={()=>console.log("clicked")} />;

const Template = (args) => <Agriculture {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
