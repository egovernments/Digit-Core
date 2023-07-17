import React from "react";
import { Input } from "./Input";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Input",
  component: Input,
};

export const Default = () => <Input />;
export const Fill = () => <Input fill="blue" />;
export const Size = () => <Input height="50" width="50" />;
export const CustomStyle = () => <Input style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Input className="custom-class" />;

export const Clickable = () => <Input onClick={()=>console.log("clicked")} />;

const Template = (args) => <Input {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
