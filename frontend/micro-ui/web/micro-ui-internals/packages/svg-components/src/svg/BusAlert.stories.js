import React from "react";
import { BusAlert } from "./BusAlert";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "BusAlert",
  component: BusAlert,
};

export const Default = () => <BusAlert />;
export const Fill = () => <BusAlert fill="blue" />;
export const Size = () => <BusAlert height="50" width="50" />;
export const CustomStyle = () => <BusAlert style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <BusAlert className="custom-class" />;
