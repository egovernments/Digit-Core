import React from "react";
import { Explore } from "./Explore";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Explore",
  component: Explore,
};

export const Default = () => <Explore />;
export const Fill = () => <Explore fill="blue" />;
export const Size = () => <Explore height="50" width="50" />;
export const CustomStyle = () => <Explore style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Explore className="custom-class" />;
