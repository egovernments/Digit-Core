import React from "react";
import { ArrowDownward } from "./ArrowDownward";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ArrowDownward",
  component: ArrowDownward,
};

export const Default = () => <ArrowDownward />;
export const Fill = () => <ArrowDownward fill="blue" />;
export const Size = () => <ArrowDownward height="50" width="50" />;
export const CustomStyle = () => <ArrowDownward style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ArrowDownward className="custom-class" />;
export const Clickable = () => <ArrowDownward onClick={()=>console.log("clicked")} />;

const Template = (args) => <ArrowDownward {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
