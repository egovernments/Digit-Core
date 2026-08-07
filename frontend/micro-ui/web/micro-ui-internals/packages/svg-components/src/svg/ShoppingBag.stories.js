import React from "react";
import { ShoppingBag } from "./ShoppingBag";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ShoppingBag",
  component: ShoppingBag,
};

export const Default = () => <ShoppingBag />;
export const Fill = () => <ShoppingBag fill="blue" />;
export const Size = () => <ShoppingBag height="50" width="50" />;
export const CustomStyle = () => <ShoppingBag style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ShoppingBag className="custom-class" />;

export const Clickable = () => <ShoppingBag onClick={()=>console.log("clicked")} />;

const Template = (args) => <ShoppingBag {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
