import React from "react";
import { CloudQueue } from "./CloudQueue";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "CloudQueue",
  component: CloudQueue,
};

export const Default = () => <CloudQueue />;
export const Fill = () => <CloudQueue fill="blue" />;
export const Size = () => <CloudQueue height="50" width="50" />;
export const CustomStyle = () => <CloudQueue style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <CloudQueue className="custom-class" />;
