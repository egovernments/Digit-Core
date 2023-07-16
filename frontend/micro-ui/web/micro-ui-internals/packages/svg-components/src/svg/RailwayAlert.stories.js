import React from "react";
import { RailwayAlert } from "./RailwayAlert";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "RailwayAlert",
  component: RailwayAlert,
};

export const Default = () => <RailwayAlert />;
export const Fill = () => <RailwayAlert fill="blue" />;
export const Size = () => <RailwayAlert height="50" width="50" />;
export const CustomStyle = () => <RailwayAlert style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <RailwayAlert className="custom-class" />;
