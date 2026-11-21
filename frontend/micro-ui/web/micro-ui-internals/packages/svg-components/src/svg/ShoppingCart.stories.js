import React from "react";
import { ShoppingCart } from "./ShoppingCart";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ShoppingCart",
  component: ShoppingCart,
};

export const Default = () => <ShoppingCart />;
export const Fill = () => <ShoppingCart fill="blue" />;
export const Size = () => <ShoppingCart height="50" width="50" />;
export const CustomStyle = () => <ShoppingCart style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ShoppingCart className="custom-class" />;

export const Clickable = () => <ShoppingCart onClick={()=>console.log("clicked")} />;

const Template = (args) => <ShoppingCart {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
