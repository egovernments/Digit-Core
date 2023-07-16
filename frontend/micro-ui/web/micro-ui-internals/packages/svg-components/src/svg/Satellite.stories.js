import React from "react";
import { Satellite } from "./Satellite";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Satellite",
  component: Satellite,
};

export const Default = () => <Satellite />;
export const Fill = () => <Satellite fill="blue" />;
export const Size = () => <Satellite height="50" width="50" />;
export const CustomStyle = () => <Satellite style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Satellite className="custom-class" />;
