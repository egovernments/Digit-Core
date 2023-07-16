import React from "react";
import { Segment } from "./Segment";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Segment",
  component: Segment,
};

export const Default = () => <Segment />;
export const Fill = () => <Segment fill="blue" />;
export const Size = () => <Segment height="50" width="50" />;
export const CustomStyle = () => <Segment style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Segment className="custom-class" />;
