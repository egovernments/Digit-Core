import React from "react";
import { PersonAdd } from "./PersonAdd";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PersonAdd",
  component: PersonAdd,
};

export const Default = () => <PersonAdd />;
export const Fill = () => <PersonAdd fill="blue" />;
export const Size = () => <PersonAdd height="50" width="50" />;
export const CustomStyle = () => <PersonAdd style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PersonAdd className="custom-class" />;
