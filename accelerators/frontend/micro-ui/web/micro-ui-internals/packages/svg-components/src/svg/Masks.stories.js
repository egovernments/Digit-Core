import React from "react";
import { Masks } from "./Masks";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Masks",
  component: Masks,
};

export const Default = () => <Masks />;
export const Fill = () => <Masks fill="blue" />;
export const Size = () => <Masks height="50" width="50" />;
export const CustomStyle = () => <Masks style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Masks className="custom-class" />;
