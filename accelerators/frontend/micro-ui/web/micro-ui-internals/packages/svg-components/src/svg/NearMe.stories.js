import React from "react";
import { NearMe } from "./NearMe";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "NearMe",
  component: NearMe,
};

export const Default = () => <NearMe />;
export const Fill = () => <NearMe fill="blue" />;
export const Size = () => <NearMe height="50" width="50" />;
export const CustomStyle = () => <NearMe style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <NearMe className="custom-class" />;
