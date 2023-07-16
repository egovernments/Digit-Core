import React from "react";
import { PersonOutline } from "./PersonOutline";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PersonOutline",
  component: PersonOutline,
};

export const Default = () => <PersonOutline />;
export const Fill = () => <PersonOutline fill="blue" />;
export const Size = () => <PersonOutline height="50" width="50" />;
export const CustomStyle = () => <PersonOutline style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PersonOutline className="custom-class" />;
