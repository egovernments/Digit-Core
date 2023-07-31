import React from "react";
import { ShoppingBasket } from "./ShoppingBasket";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ShoppingBasket",
  component: ShoppingBasket,
};

export const Default = () => <ShoppingBasket />;
export const Fill = () => <ShoppingBasket fill="blue" />;
export const Size = () => <ShoppingBasket height="50" width="50" />;
export const CustomStyle = () => <ShoppingBasket style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ShoppingBasket className="custom-class" />;

export const Clickable = () => <ShoppingBasket onClick={()=>console.log("clicked")} />;

const Template = (args) => <ShoppingBasket {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
