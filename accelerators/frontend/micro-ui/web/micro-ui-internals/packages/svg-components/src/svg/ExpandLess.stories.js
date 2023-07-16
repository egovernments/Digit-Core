import React from "react";
import { ExpandLess } from "./ExpandLess";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ExpandLess",
  component: ExpandLess,
};

export const Default = () => <ExpandLess />;
export const Fill = () => <ExpandLess fill="blue" />;
export const Size = () => <ExpandLess height="50" width="50" />;
export const CustomStyle = () => <ExpandLess style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ExpandLess className="custom-class" />;
