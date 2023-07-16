import React from "react";
import { NotStarted } from "./NotStarted";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "NotStarted",
  component: NotStarted,
};

export const Default = () => <NotStarted />;
export const Fill = () => <NotStarted fill="blue" />;
export const Size = () => <NotStarted height="50" width="50" />;
export const CustomStyle = () => <NotStarted style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <NotStarted className="custom-class" />;
