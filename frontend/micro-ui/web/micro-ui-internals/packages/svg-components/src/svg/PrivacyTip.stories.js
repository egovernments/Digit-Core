import React from "react";
import { PrivacyTip } from "./PrivacyTip";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PrivacyTip",
  component: PrivacyTip,
};

export const Default = () => <PrivacyTip />;
export const Fill = () => <PrivacyTip fill="blue" />;
export const Size = () => <PrivacyTip height="50" width="50" />;
export const CustomStyle = () => <PrivacyTip style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PrivacyTip className="custom-class" />;
