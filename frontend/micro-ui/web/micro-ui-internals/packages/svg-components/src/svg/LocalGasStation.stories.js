import React from "react";
import { LocalGasStation } from "./LocalGasStation";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LocalGasStation",
  component: LocalGasStation,
};

export const Default = () => <LocalGasStation />;
export const Fill = () => <LocalGasStation fill="blue" />;
export const Size = () => <LocalGasStation height="50" width="50" />;
export const CustomStyle = () => <LocalGasStation style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LocalGasStation className="custom-class" />;
