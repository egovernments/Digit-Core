import React from "react";
import { StayCurrentLandscape } from "./StayCurrentLandscape";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "StayCurrentLandscape",
  component: StayCurrentLandscape,
};

export const Default = () => <StayCurrentLandscape />;
export const Fill = () => <StayCurrentLandscape fill="blue" />;
export const Size = () => <StayCurrentLandscape height="50" width="50" />;
export const CustomStyle = () => <StayCurrentLandscape style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <StayCurrentLandscape className="custom-class" />;
