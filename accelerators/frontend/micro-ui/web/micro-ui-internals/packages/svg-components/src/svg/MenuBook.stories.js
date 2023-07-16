import React from "react";
import { MenuBook } from "./MenuBook";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "MenuBook",
  component: MenuBook,
};

export const Default = () => <MenuBook />;
export const Fill = () => <MenuBook fill="blue" />;
export const Size = () => <MenuBook height="50" width="50" />;
export const CustomStyle = () => <MenuBook style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <MenuBook className="custom-class" />;
