import React from "react";
import { Recommend } from "./Recommend";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Recommend",
  component: Recommend,
};

export const Default = () => <Recommend />;
export const Fill = () => <Recommend fill="blue" />;
export const Size = () => <Recommend height="50" width="50" />;
export const CustomStyle = () => <Recommend style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Recommend className="custom-class" />;
