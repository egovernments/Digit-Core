import React from "react";
import { PersonRemove } from "./PersonRemove";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PersonRemove",
  component: PersonRemove,
};

export const Default = () => <PersonRemove />;
export const Fill = () => <PersonRemove fill="blue" />;
export const Size = () => <PersonRemove height="50" width="50" />;
export const CustomStyle = () => <PersonRemove style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PersonRemove className="custom-class" />;
