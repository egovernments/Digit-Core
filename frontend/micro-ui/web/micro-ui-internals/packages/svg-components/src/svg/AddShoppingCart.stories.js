import React from "react";
import { AddShoppingCart } from "./AddShoppingCart";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "AddShoppingCart",
  component: AddShoppingCart,
};

export const Default = () => <AddShoppingCart />;
export const Fill = () => <AddShoppingCart fill="blue" />;
export const Size = () => <AddShoppingCart height="50" width="50" />;
export const CustomStyle = () => <AddShoppingCart style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AddShoppingCart className="custom-class" />;
export const Clickable = () => <AddShoppingCart onClick={()=>console.log("clicked")} />;

const Template = (args) => <AddShoppingCart {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
