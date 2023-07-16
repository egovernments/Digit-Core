import React from "react";
import { ExploreOff } from "./ExploreOff";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ExploreOff",
  component: ExploreOff,
};

export const Default = () => <ExploreOff />;
export const Fill = () => <ExploreOff fill="blue" />;
export const Size = () => <ExploreOff height="50" width="50" />;
export const CustomStyle = () => <ExploreOff style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ExploreOff className="custom-class" />;
