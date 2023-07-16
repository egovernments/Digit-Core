import React from "react";
import { CellWifi } from "./CellWifi";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "CellWifi",
  component: CellWifi,
};

export const Default = () => <CellWifi />;
export const Fill = () => <CellWifi fill="blue" />;
export const Size = () => <CellWifi height="50" width="50" />;
export const CustomStyle = () => <CellWifi style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <CellWifi className="custom-class" />;
