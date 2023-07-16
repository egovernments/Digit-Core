import React from "react";
import { Rotation3D } from "./Rotation3D";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Rotation3D",
  component: Rotation3D,
};

export const Default = () => <Rotation3D />;
export const Fill = () => <Rotation3D fill="blue" />;
export const Size = () => <Rotation3D height="50" width="50" />;
export const CustomStyle = () => <Rotation3D style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Rotation3D className="custom-class" />;
