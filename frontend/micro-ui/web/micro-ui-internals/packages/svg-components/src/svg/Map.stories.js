import React from "react";
import { Map } from "./Map";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Map",
  component: Map,
};

export const Default = () => <Map />;
export const Fill = () => <Map fill="blue" />;
export const Size = () => <Map height="50" width="50" />;
export const CustomStyle = () => <Map style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Map className="custom-class" />;
