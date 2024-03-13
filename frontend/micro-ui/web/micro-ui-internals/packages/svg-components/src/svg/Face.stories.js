import React from "react";
import { Face } from "./Face";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Face",
  component: Face,
};

export const Default = () => <Face />;
export const Fill = () => <Face fill="blue" />;
export const Size = () => <Face height="50" width="50" />;
export const CustomStyle = () => <Face style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Face className="custom-class" />;

export const Clickable = () => <Face onClick={()=>console.log("clicked")} />;

const Template = (args) => <Face {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
