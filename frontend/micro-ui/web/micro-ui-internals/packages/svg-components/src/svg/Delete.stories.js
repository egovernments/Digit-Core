import React from "react";
import { Delete } from "./Delete";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Delete",
  component: Delete,
};

export const Default = () => <Delete />;
export const Fill = () => <Delete fill="blue" />;
export const Size = () => <Delete height="50" width="50" />;
export const CustomStyle = () => <Delete style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Delete className="custom-class" />;

export const Clickable = () => <Delete onClick={()=>console.log("clicked")} />;

const Template = (args) => <Delete {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
