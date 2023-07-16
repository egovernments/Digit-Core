import React from "react";
import { OutdoorGrill } from "./OutdoorGrill";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "OutdoorGrill",
  component: OutdoorGrill,
};

export const Default = () => <OutdoorGrill />;
export const Fill = () => <OutdoorGrill fill="blue" />;
export const Size = () => <OutdoorGrill height="50" width="50" />;
export const CustomStyle = () => <OutdoorGrill style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <OutdoorGrill className="custom-class" />;
