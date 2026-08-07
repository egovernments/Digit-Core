import React from "react";
import { School } from "./School";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "School",
  component: School,
};

export const Default = () => <School />;
export const Fill = () => <School fill="blue" />;
export const Size = () => <School height="50" width="50" />;
export const CustomStyle = () => <School style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <School className="custom-class" />;

export const Clickable = () => <School onClick={()=>console.log("clicked")} />;

const Template = (args) => <School {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
