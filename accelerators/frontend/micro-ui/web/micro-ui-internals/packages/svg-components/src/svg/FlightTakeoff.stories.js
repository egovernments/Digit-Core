import React from "react";
import { FlightTakeoff } from "./FlightTakeoff";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "FlightTakeoff",
  component: FlightTakeoff,
};

export const Default = () => <FlightTakeoff />;
export const Fill = () => <FlightTakeoff fill="blue" />;
export const Size = () => <FlightTakeoff height="50" width="50" />;
export const CustomStyle = () => <FlightTakeoff style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <FlightTakeoff className="custom-class" />;
