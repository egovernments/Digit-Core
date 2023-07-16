import React from "react";
import { ShopAlt } from "./ShopAlt";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ShopAlt",
  component: ShopAlt,
};

export const Default = () => <ShopAlt />;
export const Fill = () => <ShopAlt fill="blue" />;
export const Size = () => <ShopAlt height="50" width="50" />;
export const CustomStyle = () => <ShopAlt style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ShopAlt className="custom-class" />;
