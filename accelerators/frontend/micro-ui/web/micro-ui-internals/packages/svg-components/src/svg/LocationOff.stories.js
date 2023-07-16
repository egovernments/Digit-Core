import React from "react";
import { LocationOff } from "./LocationOff";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LocationOff",
  component: LocationOff,
};

export const Default = () => <LocationOff />;
export const Fill = () => <LocationOff fill="blue" />;
export const Size = () => <LocationOff height="50" width="50" />;
export const CustomStyle = () => <LocationOff style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LocationOff className="custom-class" />;
