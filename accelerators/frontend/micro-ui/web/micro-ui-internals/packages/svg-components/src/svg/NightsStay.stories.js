import React from "react";
import { NightsStay } from "./NightsStay";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "NightsStay",
  component: NightsStay,
};

export const Default = () => <NightsStay />;
export const Fill = () => <NightsStay fill="blue" />;
export const Size = () => <NightsStay height="50" width="50" />;
export const CustomStyle = () => <NightsStay style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <NightsStay className="custom-class" />;
