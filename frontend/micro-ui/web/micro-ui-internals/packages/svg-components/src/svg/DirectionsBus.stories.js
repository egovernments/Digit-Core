import React from "react";
import { DirectionsBus } from "./DirectionsBus";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "DirectionsBus",
  component: DirectionsBus,
};

export const Default = () => <DirectionsBus />;
export const Fill = () => <DirectionsBus fill="blue" />;
export const Size = () => <DirectionsBus height="50" width="50" />;
export const CustomStyle = () => <DirectionsBus style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <DirectionsBus className="custom-class" />;
