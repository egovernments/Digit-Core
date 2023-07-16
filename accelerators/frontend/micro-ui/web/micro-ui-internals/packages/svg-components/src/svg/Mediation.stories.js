import React from "react";
import { Mediation } from "./Mediation";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Mediation",
  component: Mediation,
};

export const Default = () => <Mediation />;
export const Fill = () => <Mediation fill="blue" />;
export const Size = () => <Mediation height="50" width="50" />;
export const CustomStyle = () => <Mediation style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Mediation className="custom-class" />;
