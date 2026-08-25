import React from "react";
import { CheckCircle } from "./CheckCircle";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "CheckCircle",
  component: CheckCircle,
};

export const Default = () => <CheckCircle />;
export const Fill = () => <CheckCircle fill="blue" />;
export const Size = () => <CheckCircle height="50" width="50" />;
export const CustomStyle = () => <CheckCircle style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <CheckCircle className="custom-class" />;

export const Clickable = () => <CheckCircle onClick={()=>console.log("clicked")} />;

const Template = (args) => <CheckCircle {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
