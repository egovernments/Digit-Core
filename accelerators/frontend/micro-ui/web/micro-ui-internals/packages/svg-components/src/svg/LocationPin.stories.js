import React from "react";
import { LocationPin } from "./LocationPin";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LocationPin",
  component: LocationPin,
};

export const Default = () => <LocationPin />;
export const Fill = () => <LocationPin fill="blue" />;
export const Size = () => <LocationPin height="50" width="50" />;
export const CustomStyle = () => <LocationPin style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LocationPin className="custom-class" />;
