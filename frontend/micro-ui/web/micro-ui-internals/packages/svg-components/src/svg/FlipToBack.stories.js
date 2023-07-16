import React from "react";
import { FlipToBack } from "./FlipToBack";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "FlipToBack",
  component: FlipToBack,
};

export const Default = () => <FlipToBack />;
export const Fill = () => <FlipToBack fill="blue" />;
export const Size = () => <FlipToBack height="50" width="50" />;
export const CustomStyle = () => <FlipToBack style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <FlipToBack className="custom-class" />;
