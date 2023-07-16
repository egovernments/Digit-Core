import React from "react";
import { CleaningServices } from "./CleaningServices";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "CleaningServices",
  component: CleaningServices,
};

export const Default = () => <CleaningServices />;
export const Fill = () => <CleaningServices fill="blue" />;
export const Size = () => <CleaningServices height="50" width="50" />;
export const CustomStyle = () => <CleaningServices style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <CleaningServices className="custom-class" />;
