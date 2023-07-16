import React from "react";
import { Polymer } from "./Polymer";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Polymer",
  component: Polymer,
};

export const Default = () => <Polymer />;
export const Fill = () => <Polymer fill="blue" />;
export const Size = () => <Polymer height="50" width="50" />;
export const CustomStyle = () => <Polymer style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Polymer className="custom-class" />;
