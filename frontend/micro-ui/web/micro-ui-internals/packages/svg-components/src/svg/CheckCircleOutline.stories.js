import React from "react";
import { CheckCircleOutline } from "./CheckCircleOutline";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "CheckCircleOutline",
  component: CheckCircleOutline,
};

export const Default = () => <CheckCircleOutline />;
export const Fill = () => <CheckCircleOutline fill="blue" />;
export const Size = () => <CheckCircleOutline height="50" width="50" />;
export const CustomStyle = () => <CheckCircleOutline style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <CheckCircleOutline className="custom-class" />;

export const Clickable = () => <CheckCircleOutline onClick={()=>console.log("clicked")} />;

const Template = (args) => <CheckCircleOutline {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
