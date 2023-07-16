import React from "react";
import { SelfImprovement } from "./SelfImprovement";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SelfImprovement",
  component: SelfImprovement,
};

export const Default = () => <SelfImprovement />;
export const Fill = () => <SelfImprovement fill="blue" />;
export const Size = () => <SelfImprovement height="50" width="50" />;
export const CustomStyle = () => <SelfImprovement style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SelfImprovement className="custom-class" />;
