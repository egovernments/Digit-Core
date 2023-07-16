import React from "react";
import { DirectionsTransit } from "./DirectionsTransit";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "DirectionsTransit",
  component: DirectionsTransit,
};

export const Default = () => <DirectionsTransit />;
export const Fill = () => <DirectionsTransit fill="blue" />;
export const Size = () => <DirectionsTransit height="50" width="50" />;
export const CustomStyle = () => <DirectionsTransit style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <DirectionsTransit className="custom-class" />;
