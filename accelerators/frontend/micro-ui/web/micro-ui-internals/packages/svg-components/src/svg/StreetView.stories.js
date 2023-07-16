import React from "react";
import { StreetView } from "./StreetView";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "StreetView",
  component: StreetView,
};

export const Default = () => <StreetView />;
export const Fill = () => <StreetView fill="blue" />;
export const Size = () => <StreetView height="50" width="50" />;
export const CustomStyle = () => <StreetView style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <StreetView className="custom-class" />;
