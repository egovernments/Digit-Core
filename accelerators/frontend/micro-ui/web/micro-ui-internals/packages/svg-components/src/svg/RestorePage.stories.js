import React from "react";
import { RestorePage } from "./RestorePage";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "RestorePage",
  component: RestorePage,
};

export const Default = () => <RestorePage />;
export const Fill = () => <RestorePage fill="blue" />;
export const Size = () => <RestorePage height="50" width="50" />;
export const CustomStyle = () => <RestorePage style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <RestorePage className="custom-class" />;
