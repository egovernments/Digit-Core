import React from "react";
import { Shop } from "./Shop";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Shop",
  component: Shop,
};

export const Default = () => <Shop />;
export const Fill = () => <Shop fill="blue" />;
export const Size = () => <Shop height="50" width="50" />;
export const CustomStyle = () => <Shop style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Shop className="custom-class" />;
