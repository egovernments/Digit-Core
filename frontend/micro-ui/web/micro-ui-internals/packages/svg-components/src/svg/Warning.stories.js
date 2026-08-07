import React from "react";
import { Warning } from "./Warning";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Warning",
  component: Warning,
};

export const Default = () => <Warning />;
export const Fill = () => <Warning fill="blue" />;
export const Size = () => <Warning height="50" width="50" />;
export const CustomStyle = () => <Warning style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Warning className="custom-class" />;
export const Clickable = () => <Warning onClick={()=>console.log("clicked")} />;

const Template = (args) => <Warning {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
