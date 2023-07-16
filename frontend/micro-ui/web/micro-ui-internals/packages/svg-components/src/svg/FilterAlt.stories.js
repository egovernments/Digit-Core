import React from "react";
import { FilterAlt } from "./FilterAlt";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "FilterAlt",
  component: FilterAlt,
};

export const Default = () => <FilterAlt />;
export const Fill = () => <FilterAlt fill="blue" />;
export const Size = () => <FilterAlt height="50" width="50" />;
export const CustomStyle = () => <FilterAlt style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <FilterAlt className="custom-class" />;
