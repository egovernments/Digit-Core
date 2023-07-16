import React from "react";
import { LocationCity } from "./LocationCity";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LocationCity",
  component: LocationCity,
};

export const Default = () => <LocationCity />;
export const Fill = () => <LocationCity fill="blue" />;
export const Size = () => <LocationCity height="50" width="50" />;
export const CustomStyle = () => <LocationCity style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LocationCity className="custom-class" />;
