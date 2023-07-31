import React from "react";
import { ArrowDropUp } from "./ArrowDropUp";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ArrowDropUp",
  component: ArrowDropUp,
};

export const Default = () => <ArrowDropUp />;
export const Fill = () => <ArrowDropUp fill="blue" />;
export const Size = () => <ArrowDropUp height="50" width="50" />;
export const CustomStyle = () => <ArrowDropUp style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ArrowDropUp className="custom-class" />;
export const Clickable = () => <ArrowDropUp onClick={()=>console.log("clicked")} />;

const Template = (args) => <ArrowDropUp {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
