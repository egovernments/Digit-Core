import React from "react";
import { FollowTheSigns } from "./FollowTheSigns";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "FollowTheSigns",
  component: FollowTheSigns,
};

export const Default = () => <FollowTheSigns />;
export const Fill = () => <FollowTheSigns fill="blue" />;
export const Size = () => <FollowTheSigns height="50" width="50" />;
export const CustomStyle = () => <FollowTheSigns style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <FollowTheSigns className="custom-class" />;
