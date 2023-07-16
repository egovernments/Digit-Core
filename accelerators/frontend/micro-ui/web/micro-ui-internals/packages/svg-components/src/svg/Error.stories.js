import React from "react";
import { Error } from "./Error";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Error",
  component: Error,
};

export const Default = () => <Error />;
export const Fill = () => <Error fill="blue" />;
export const Size = () => <Error height="50" width="50" />;
export const CustomStyle = () => <Error style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Error className="custom-class" />;
