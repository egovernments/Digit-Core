import React from "react";
import { LocalGroceryStore } from "./LocalGroceryStore";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LocalGroceryStore",
  component: LocalGroceryStore,
};

export const Default = () => <LocalGroceryStore />;
export const Fill = () => <LocalGroceryStore fill="blue" />;
export const Size = () => <LocalGroceryStore height="50" width="50" />;
export const CustomStyle = () => <LocalGroceryStore style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LocalGroceryStore className="custom-class" />;

export const Clickable = () => <LocalGroceryStore onClick={()=>console.log("clicked")} />;

const Template = (args) => <LocalGroceryStore {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
