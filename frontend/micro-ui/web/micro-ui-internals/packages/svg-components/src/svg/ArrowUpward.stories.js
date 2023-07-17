import React from "react";
import { ArrowUpward } from "./ArrowUpward";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ArrowUpward",
  component: ArrowUpward,
};

export const Default = () => <ArrowUpward />;
export const Fill = () => <ArrowUpward fill="blue" />;
export const Size = () => <ArrowUpward height="50" width="50" />;
export const CustomStyle = () => <ArrowUpward style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ArrowUpward className="custom-class" />;
export const Clickable = () => <ArrowUpward onClick={()=>console.log("clicked")} />;

const Template = (args) => <ArrowUpward {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
