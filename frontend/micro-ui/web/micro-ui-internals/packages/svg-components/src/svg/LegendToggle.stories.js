import React from "react";
import { LegendToggle } from "./LegendToggle";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LegendToggle",
  component: LegendToggle,
};

export const Default = () => <LegendToggle />;
export const Fill = () => <LegendToggle fill="blue" />;
export const Size = () => <LegendToggle height="50" width="50" />;
export const CustomStyle = () => <LegendToggle style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LegendToggle className="custom-class" />;
