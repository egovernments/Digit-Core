import React from "react";
import { Assignment } from "./Assignment";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Assignment",
  component: Assignment,
};

export const Default = () => <Assignment />;
export const Fill = () => <Assignment fill="blue" />;
export const Size = () => <Assignment height="50" width="50" />;
export const CustomStyle = () => <Assignment style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Assignment className="custom-class" />;
export const Clickable = () => <Assignment onClick={()=>console.log("clicked")} />;

const Template = (args) => <Assignment {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
