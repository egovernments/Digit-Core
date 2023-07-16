import React from "react";
import { DirectionsWalk } from "./DirectionsWalk";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "DirectionsWalk",
  component: DirectionsWalk,
};

export const Default = () => <DirectionsWalk />;
export const Fill = () => <DirectionsWalk fill="blue" />;
export const Size = () => <DirectionsWalk height="50" width="50" />;
export const CustomStyle = () => <DirectionsWalk style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <DirectionsWalk className="custom-class" />;
