import React from "react";
import { TrendingUp } from "./TrendingUp";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "TrendingUp",
  component: TrendingUp,
};

export const Default = () => <TrendingUp />;
export const Fill = () => <TrendingUp fill="blue" />;
export const Size = () => <TrendingUp height="50" width="50" />;
export const CustomStyle = () => <TrendingUp style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TrendingUp className="custom-class" />;
export const Clickable = () => <TrendingUp onClick={()=>console.log("clicked")} />;

const Template = (args) => <TrendingUp {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
