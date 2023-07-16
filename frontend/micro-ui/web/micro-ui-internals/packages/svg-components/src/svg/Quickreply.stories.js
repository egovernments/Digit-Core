import React from "react";
import { Quickreply } from "./Quickreply";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Quickreply",
  component: Quickreply,
};

export const Default = () => <Quickreply />;
export const Fill = () => <Quickreply fill="blue" />;
export const Size = () => <Quickreply height="50" width="50" />;
export const CustomStyle = () => <Quickreply style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Quickreply className="custom-class" />;
