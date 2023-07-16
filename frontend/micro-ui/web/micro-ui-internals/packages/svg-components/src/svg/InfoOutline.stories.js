import React from "react";
import { InfoOutline } from "./InfoOutline";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "InfoOutline",
  component: InfoOutline,
};

export const Default = () => <InfoOutline />;
export const Fill = () => <InfoOutline fill="blue" />;
export const Size = () => <InfoOutline height="50" width="50" />;
export const CustomStyle = () => <InfoOutline style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <InfoOutline className="custom-class" />;
