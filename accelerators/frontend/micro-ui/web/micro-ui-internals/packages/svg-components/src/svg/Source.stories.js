import React from "react";
import { Source } from "./Source";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Source",
  component: Source,
};

export const Default = () => <Source />;
export const Fill = () => <Source fill="blue" />;
export const Size = () => <Source height="50" width="50" />;
export const CustomStyle = () => <Source style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Source className="custom-class" />;
