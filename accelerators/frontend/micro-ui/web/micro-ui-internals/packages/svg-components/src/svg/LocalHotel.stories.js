import React from "react";
import { LocalHotel } from "./LocalHotel";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LocalHotel",
  component: LocalHotel,
};

export const Default = () => <LocalHotel />;
export const Fill = () => <LocalHotel fill="blue" />;
export const Size = () => <LocalHotel height="50" width="50" />;
export const CustomStyle = () => <LocalHotel style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LocalHotel className="custom-class" />;
