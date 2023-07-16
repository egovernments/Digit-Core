import React from "react";
import { DisabledByDefault } from "./DisabledByDefault";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "DisabledByDefault",
  component: DisabledByDefault,
};

export const Default = () => <DisabledByDefault />;
export const Fill = () => <DisabledByDefault fill="blue" />;
export const Size = () => <DisabledByDefault height="50" width="50" />;
export const CustomStyle = () => <DisabledByDefault style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <DisabledByDefault className="custom-class" />;
