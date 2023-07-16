import React from "react";
import { FlightLand } from "./FlightLand";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "FlightLand",
  component: FlightLand,
};

export const Default = () => <FlightLand />;
export const Fill = () => <FlightLand fill="blue" />;
export const Size = () => <FlightLand height="50" width="50" />;
export const CustomStyle = () => <FlightLand style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <FlightLand className="custom-class" />;
