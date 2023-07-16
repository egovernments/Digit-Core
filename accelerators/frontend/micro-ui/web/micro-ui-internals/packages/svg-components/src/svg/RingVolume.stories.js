import React from "react";
import { RingVolume } from "./RingVolume";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "RingVolume",
  component: RingVolume,
};

export const Default = () => <RingVolume />;
export const Fill = () => <RingVolume fill="blue" />;
export const Size = () => <RingVolume height="50" width="50" />;
export const CustomStyle = () => <RingVolume style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <RingVolume className="custom-class" />;
