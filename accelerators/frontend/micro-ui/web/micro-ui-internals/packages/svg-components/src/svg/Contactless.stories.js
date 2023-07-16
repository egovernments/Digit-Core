import React from "react";
import { Contactless } from "./Contactless";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Contactless",
  component: Contactless,
};

export const Default = () => <Contactless />;
export const Fill = () => <Contactless fill="blue" />;
export const Size = () => <Contactless height="50" width="50" />;
export const CustomStyle = () => <Contactless style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Contactless className="custom-class" />;
