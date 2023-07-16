import React from "react";
import { RecordVoiceOver } from "./RecordVoiceOver";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "RecordVoiceOver",
  component: RecordVoiceOver,
};

export const Default = () => <RecordVoiceOver />;
export const Fill = () => <RecordVoiceOver fill="blue" />;
export const Size = () => <RecordVoiceOver height="50" width="50" />;
export const CustomStyle = () => <RecordVoiceOver style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <RecordVoiceOver className="custom-class" />;
