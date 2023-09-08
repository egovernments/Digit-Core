import React from "react";
import { DoubleArrow } from "./DoubleArrow";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "DoubleArrow",
  component: DoubleArrow,
};

export const Default = () => <DoubleArrow />;
export const Fill = () => <DoubleArrow fill="blue" />;
export const Size = () => <DoubleArrow height="50" width="50" />;
export const CustomStyle = () => <DoubleArrow style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <DoubleArrow className="custom-class" />;

export const Clickable = () => <DoubleArrow onClick={()=>console.log("clicked")} />;

const Template = (args) => <DoubleArrow {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
