import React from "react";
import { RequestQuote } from "./RequestQuote";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "RequestQuote",
  component: RequestQuote,
};

export const Default = () => <RequestQuote />;
export const Fill = () => <RequestQuote fill="blue" />;
export const Size = () => <RequestQuote height="50" width="50" />;
export const CustomStyle = () => <RequestQuote style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <RequestQuote className="custom-class" />;
