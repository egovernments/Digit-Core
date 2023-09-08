import React from "react";
import { LocalActivity } from "./LocalActivity";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LocalActivity",
  component: LocalActivity,
};

export const Default = () => <LocalActivity />;
export const Fill = () => <LocalActivity fill="blue" />;
export const Size = () => <LocalActivity height="50" width="50" />;
export const CustomStyle = () => <LocalActivity style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LocalActivity className="custom-class" />;

export const Clickable = () => <LocalActivity onClick={()=>console.log("clicked")} />;

const Template = (args) => <LocalActivity {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
