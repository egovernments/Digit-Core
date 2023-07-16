import React from "react";
import { FindInPage } from "./FindInPage";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "FindInPage",
  component: FindInPage,
};

export const Default = () => <FindInPage />;
export const Fill = () => <FindInPage fill="blue" />;
export const Size = () => <FindInPage height="50" width="50" />;
export const CustomStyle = () => <FindInPage style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <FindInPage className="custom-class" />;
