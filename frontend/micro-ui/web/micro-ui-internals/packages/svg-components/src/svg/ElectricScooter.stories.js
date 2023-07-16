import React from "react";
import { ElectricScooter } from "./ElectricScooter";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ElectricScooter",
  component: ElectricScooter,
};

export const Default = () => <ElectricScooter />;
export const Fill = () => <ElectricScooter fill="blue" />;
export const Size = () => <ElectricScooter height="50" width="50" />;
export const CustomStyle = () => <ElectricScooter style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ElectricScooter className="custom-class" />;
