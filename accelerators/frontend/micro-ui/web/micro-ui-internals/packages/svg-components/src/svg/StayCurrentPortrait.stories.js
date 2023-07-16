import React from "react";
import { StayCurrentPortrait } from "./StayCurrentPortrait";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "StayCurrentPortrait",
  component: StayCurrentPortrait,
};

export const Default = () => <StayCurrentPortrait />;
export const Fill = () => <StayCurrentPortrait fill="blue" />;
export const Size = () => <StayCurrentPortrait height="50" width="50" />;
export const CustomStyle = () => <StayCurrentPortrait style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <StayCurrentPortrait className="custom-class" />;
