import React from "react";
import { DesignServices } from "./DesignServices";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "DesignServices",
  component: DesignServices,
};

export const Default = () => <DesignServices />;
export const Fill = () => <DesignServices fill="blue" />;
export const Size = () => <DesignServices height="50" width="50" />;
export const CustomStyle = () => <DesignServices style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <DesignServices className="custom-class" />;
