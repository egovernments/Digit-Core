import React from "react";
import { PersonPin } from "./PersonPin";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PersonPin",
  component: PersonPin,
};

export const Default = () => <PersonPin />;
export const Fill = () => <PersonPin fill="blue" />;
export const Size = () => <PersonPin height="50" width="50" />;
export const CustomStyle = () => <PersonPin style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PersonPin className="custom-class" />;
