import React from "react";
import { Check } from "./Check";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Check",
  component: Check,
};

export const Default = () => <Check />;
export const Fill = () => <Check fill="blue" />;
export const Size = () => <Check height="50" width="50" />;
export const CustomStyle = () => <Check style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Check className="custom-class" />;

export const Clickable = () => <Check onClick={()=>console.log("clicked")} />;

const Template = (args) => <Check {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
