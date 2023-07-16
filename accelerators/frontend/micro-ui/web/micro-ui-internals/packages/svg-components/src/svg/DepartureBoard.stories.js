import React from "react";
import { DepartureBoard } from "./DepartureBoard";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "DepartureBoard",
  component: DepartureBoard,
};

export const Default = () => <DepartureBoard />;
export const Fill = () => <DepartureBoard fill="blue" />;
export const Size = () => <DepartureBoard height="50" width="50" />;
export const CustomStyle = () => <DepartureBoard style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <DepartureBoard className="custom-class" />;
