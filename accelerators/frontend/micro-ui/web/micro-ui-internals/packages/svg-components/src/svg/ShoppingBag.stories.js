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
