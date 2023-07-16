import React from "react";
import { EmojiFoodBeverage } from "./EmojiFoodBeverage";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "EmojiFoodBeverage",
  component: EmojiFoodBeverage,
};

export const Default = () => <EmojiFoodBeverage />;
export const Fill = () => <EmojiFoodBeverage fill="blue" />;
export const Size = () => <EmojiFoodBeverage height="50" width="50" />;
export const CustomStyle = () => <EmojiFoodBeverage style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <EmojiFoodBeverage className="custom-class" />;
