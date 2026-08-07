import React from "react";
import { BreakfastDining } from "./BreakfastDining";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "BreakfastDining",
  component: BreakfastDining,
};

export const Default = () => <BreakfastDining />;
export const Fill = () => <BreakfastDining fill="blue" />;
export const Size = () => <BreakfastDining height="50" width="50" />;
export const CustomStyle = () => <BreakfastDining style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <BreakfastDining className="custom-class" />;

export const Clickable = () => <BreakfastDining onClick={()=>console.log("clicked")} />;

const Template = (args) => <BreakfastDining {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
