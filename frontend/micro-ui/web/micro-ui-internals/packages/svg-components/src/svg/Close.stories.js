import React from "react";
import { Close } from "./Close";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Close",
  component: Close,
};

export const Default = () => <Close />;
export const Fill = () => <Close fill="blue" />;
export const Size = () => <Close height="50" width="50" />;
export const CustomStyle = () => <Close style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Close className="custom-class" />;

export const Clickable = () => <Close onClick={()=>console.log("clicked")} />;

const Template = (args) => <Close {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
