import React from "react";
import { West } from "./West";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "West",
  component: West,
};

export const Default = () => <West />;
export const Fill = () => <West fill="blue" />;
export const Size = () => <West height="50" width="50" />;
export const CustomStyle = () => <West style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <West className="custom-class" />;
export const Clickable = () => <West onClick={()=>console.log("clicked")} />;

const Template = (args) => <West {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
