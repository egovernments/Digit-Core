import React from "react";
import { RoundedCorner } from "./RoundedCorner";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "RoundedCorner",
  component: RoundedCorner,
};

export const Default = () => <RoundedCorner />;
export const Fill = () => <RoundedCorner fill="blue" />;
export const Size = () => <RoundedCorner height="50" width="50" />;
export const CustomStyle = () => <RoundedCorner style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <RoundedCorner className="custom-class" />;

export const Clickable = () => <RoundedCorner onClick={()=>console.log("clicked")} />;

const Template = (args) => <RoundedCorner {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
