import React from "react";
import { PermIdentity } from "./PermIdentity";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PermIdentity",
  component: PermIdentity,
};

export const Default = () => <PermIdentity />;
export const Fill = () => <PermIdentity fill="blue" />;
export const Size = () => <PermIdentity height="50" width="50" />;
export const CustomStyle = () => <PermIdentity style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PermIdentity className="custom-class" />;
