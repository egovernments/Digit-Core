import React from "react";
import { ArrowForward } from "./ArrowForward";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ArrowForward",
  component: ArrowForward,
};

export const Default = () => <ArrowForward />;
export const Fill = () => <ArrowForward fill="blue" />;
export const Size = () => <ArrowForward height="50" width="50" />;
export const CustomStyle = () => <ArrowForward style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ArrowForward className="custom-class" />;
export const Clickable = () => <ArrowForward onClick={()=>console.log("clicked")} />;

const Template = (args) => <ArrowForward {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
