import React from "react";
import { IconSwerageAlt } from "./IconSwerageAlt";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "IconSwerageAlt",
  component: IconSwerageAlt,
};

export const Default = () => <IconSwerageAlt />;
export const Fill = () => <IconSwerageAlt fill="blue" />;
export const Size = () => <IconSwerageAlt height="50" width="50" />;
export const CustomStyle = () => <IconSwerageAlt style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <IconSwerageAlt className="custom-class" />;
