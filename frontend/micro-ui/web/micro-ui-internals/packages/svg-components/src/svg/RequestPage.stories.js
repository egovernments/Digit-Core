import React from "react";
import { RequestPage } from "./RequestPage";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "RequestPage",
  component: RequestPage,
};

export const Default = () => <RequestPage />;
export const Fill = () => <RequestPage fill="blue" />;
export const Size = () => <RequestPage height="50" width="50" />;
export const CustomStyle = () => <RequestPage style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <RequestPage className="custom-class" />;
