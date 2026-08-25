import React from "react";
import { ArrowCircleUp } from "./ArrowCircleUp";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ArrowCircleUp",
  component: ArrowCircleUp,
};

export const Default = () => <ArrowCircleUp />;
export const Fill = () => <ArrowCircleUp fill="blue" />;
export const Size = () => <ArrowCircleUp height="50" width="50" />;
export const CustomStyle = () => <ArrowCircleUp style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ArrowCircleUp className="custom-class" />;
export const Clickable = () => <ArrowCircleUp onClick={()=>console.log("clicked")} />;

const Template = (args) => <ArrowCircleUp {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
