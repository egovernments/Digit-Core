import React from "react";
import { ErrorOutline } from "./ErrorOutline";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ErrorOutline",
  component: ErrorOutline,
};

export const Default = () => <ErrorOutline />;
export const Fill = () => <ErrorOutline fill="blue" />;
export const Size = () => <ErrorOutline height="50" width="50" />;
export const CustomStyle = () => <ErrorOutline style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ErrorOutline className="custom-class" />;

export const Clickable = () => <ErrorOutline onClick={()=>console.log("clicked")} />;

const Template = (args) => <ErrorOutline {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
