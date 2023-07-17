import React from "react";
import { ChevronLeft } from "./ChevronLeft";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ChevronLeft",
  component: ChevronLeft,
};

export const Default = () => <ChevronLeft />;
export const Fill = () => <ChevronLeft fill="blue" />;
export const Size = () => <ChevronLeft height="50" width="50" />;
export const CustomStyle = () => <ChevronLeft style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ChevronLeft className="custom-class" />;

export const Clickable = () => <ChevronLeft onClick={()=>console.log("clicked")} />;

const Template = (args) => <ChevronLeft {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
