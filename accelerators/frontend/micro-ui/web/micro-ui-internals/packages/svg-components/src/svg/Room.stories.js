import React from "react";
import { Room } from "./Room";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Room",
  component: Room,
};

export const Default = () => <Room />;
export const Fill = () => <Room fill="blue" />;
export const Size = () => <Room height="50" width="50" />;
export const CustomStyle = () => <Room style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Room className="custom-class" />;
