import React from "react";
import { Dns } from "./Dns";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Dns",
  component: Dns,
};

export const Default = () => <Dns />;
export const Fill = () => <Dns fill="blue" />;
export const Size = () => <Dns height="50" width="50" />;
export const CustomStyle = () => <Dns style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Dns className="custom-class" />;
